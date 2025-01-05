from fastapi import FastAPI, UploadFile, File, HTTPException, Query
from pydantic import BaseModel
import pandas as pd
import json
import openai
from openai import OpenAI
import os
import shutil
import requests
import sqlite3
import torch
from datetime import datetime, timedelta
from haversine import haversine
from typing import Union

from transformers import AutoModelForSequenceClassification, AutoTokenizer

# 0. load key file------------------

def register_key(key):
  os.environ['OPENAI_API_KEY'] = key
  
def load_file(filepath):
  with open(filepath, 'r') as file:
      return file.readline().strip()

# 1-1 audio2text--------------------

def audio_to_text(audio_path, filename):
    # OpenAI 클라이언트 생성
    client = OpenAI()

    # 오디오 파일을 읽어서, 위스퍼를 사용한 변환
    filename = filename
    audio_file = open(audio_path + filename, "rb")
    transcript = client.audio.transcriptions.create(
        file=audio_file,
        model="whisper-1",
        language="ko",
        response_format="text",
    )

    # 결과 반환
    return transcript

# 1-2 text2summary------------------

def text_summary(input_text):
    # OpenAI 클라이언트 생성
    client = OpenAI()

    # 시스템 역할과 응답 형식 지정
    system_role = '''
                   너는 전화에서 핵심을 요약하는 어시스턴트야.
                   횡설수설하는 전화내용에서 핵심 키워드만 추려내야 해.
                   그외에는 내용에 포함해선 안돼
                   응답은 다음의 형식을 지켜줘.
                   {"summary": \"텍스트 요약\"}.
                   '''

    # 입력데이터를 GPT-3.5-turbo에 전달하고 답변 받아오기
    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": input_text
            }
        ]
    )

    # 응답형식을 정리하고 return
    return response.choices[0].message.content

# 2. model prediction------------------

def predict(text, model, tokenizer):
    # 모델이 있는 디바이스 확인
    device = next(model.parameters()).device

    # 입력 문장 토크나이징
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)

    # 각 텐서를 모델과 같은 디바이스로 이동
    inputs = {key: value.to(device) for key, value in inputs.items()}

    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)

    # 로짓을 소프트맥스로 변환하여 확률 계산
    logits = outputs.logits
    probabilities = logits.softmax(dim=1)

    # 가장 높은 확률을 가진 클래스 선택
    pred = torch.argmax(probabilities, dim=-1).item()

    return pred+1, probabilities


# 3-1. get_distance_time------------------

def get_distance_time(start_lat, start_lng, dest_lat, dest_lng, c_id, c_key):

    url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"
    headers = {
        "X-NCP-APIGW-API-KEY-ID": c_id,
        "X-NCP-APIGW-API-KEY": c_key,
    }
    params = {
        "start": f"{start_lng},{start_lat}",  # 출발지 (경도, 위도)
        "goal": f"{dest_lng},{dest_lat}",    # 목적지 (경도, 위도)
        "option": "trafast"  # 실시간 빠른 길 옵션
    }

    # 요청하고, 답변 받아오기
    response = requests.get(url=url, headers=headers, params=params)
    flag = True

    if response.status_code == 200 :
        data = response.json()
        code = data['code']
        if code == 0 :
            pass
        elif code == 1 :
            print("요청 처리 실패. 출발지와 도착지가 동일함")
            flag = False
        elif code == 2 :
            print("요청 처리 실패. 출발지 또는 도착지가 도로 주변이 아님")
            flag = False
        elif code == 3 :
            print("요청 처리 실패. 자동차 길 찾기 결과 제공 불가")
            flag = False
        elif code == 4 :
            print("요청 처리 실패. 경유지가 도로 주변이 아님")
            flag = False
        else :
            print("요청 처리 실패. 경유지를 포함한 직선거리 합이 1500 km 이상인 경로가 요청됨")
            flag = False
    else:
      print("Error:", response.status_code)
      flag = False


    if flag == False :
        return 1000000007, 0, 0
    else :
        dist = data['route']['trafast'][0]['summary']['distance']  # m(미터)
        current_time = datetime.now()
        duration = data['route']['trafast'][0]['summary']['duration']  # ms(밀리초)
        arrival_time = current_time + timedelta(milliseconds=duration)
        arrival_time = arrival_time.strftime('%H:%M')

    return dist / 1000, arrival_time, duration

# 3-2. get_haversine------------------

def get_haversine(y, x, ty, tx):
    return haversine((y, x), (ty, tx), unit='km')

# 3-3. recommendation------------------

# 도로명 주소를 위도/경도로 변환하는 함수 (Naver API)
def get_coordinates_from_address(address, c_id, c_key):
    url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode"
    headers = {
        "X-NCP-APIGW-API-KEY-ID": c_id,
        "X-NCP-APIGW-API-KEY": c_key,
    }
    params = {"query": address}

    response = requests.get(url, headers=headers, params=params)
    if response.status_code == 200:
        data = response.json()
        if data.get("addresses"):
            lat = float(data["addresses"][0]["y"])
            lon = float(data["addresses"][0]["x"])
            return lat, lon
        else:
            raise HTTPException(status_code=404, detail="주소를 찾을 수 없습니다.")
    else:
        raise HTTPException(status_code=response.status_code, detail="Naver API 요청 실패")


def seconds_to_minutes_seconds(seconds):
    # 밀리초 -> 분, 초
    minutes, seconds = divmod(seconds / 1000, 60)
    return f"{int(minutes)}분 {int(seconds)}초"


def recommend_hospital3(start_lat, start_lng, emergency, c_id, c_key, k) :
    search_range = 0.05
    
    temp = emergency.loc[emergency['위도'].between(start_lat-search_range, start_lat+search_range) & emergency['경도'].between(start_lng-search_range, start_lng+search_range)].copy()

    temp['거리'], temp['도착예정시간'], temp['소요시간'] = zip(*temp.apply(lambda x: get_distance_time(start_lat, start_lng, x['위도'], x['경도'], c_id, c_key), axis=1))
    temp['소요시간'] = temp['소요시간'].apply(seconds_to_minutes_seconds)
    sorted_temp =  temp.sort_values(by='거리').head(k)
    # 결과를 JSON 형태로 변환
    result_json = sorted_temp.apply(lambda row: {
        "hospitalName": row["병원이름"],
        "address": row["주소"],
        "emergencyMedicalInstitutionType": row["응급의료기관 종류"],
        "phoneNumber1": row["전화번호 1"],
        "phoneNumber3": row["전화번호 3"],
        "latitude": row["위도"],
        "longitude": row["경도"],
        "distance": row["거리"],
        "arrivalTime": row["도착예정시간"],
        "duration": row["소요시간"]
    }, axis=1).tolist()
    
    return result_json


def insert_db(input_text, predict_class, latitude, longitude, path, result):
    dt = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    conn = sqlite3.connect(path)

    data = pd.DataFrame({
        'datetime': [dt],
        'input_text': [input_text],
        'input_latitute': [latitude], 
        'input_longitude': [longitude],
        'em_class': [predict_class], 
        'hospital1': [result[0]['hospitalName']],
        'addr1': [result[0]['address']],
        'tel1': [result[0]['phoneNumber1']],
        'dist1': [result[0]['distance']],
        'arrival1': [result[0]['arrivalTime']],
        'duration1': [result[0]['duration']],
        'hospital2': [result[1]['hospitalName']],
        'addr2': [result[1]['address']],
        'tel2': [result[1]['phoneNumber1']],
        'dist2': [result[1]['distance']],
        'arrival2': [result[1]['arrivalTime']],
        'duration2': [result[1]['duration']],
        'hospital3': [result[2]['hospitalName']],
        'addr3': [result[2]['address']],
        'tel3': [result[2]['phoneNumber1']],
        'dist3': [result[2]['distance']],
        'arrival3': [result[2]['arrivalTime']],
        'duration3': [result[2]['duration']]
    })
    data.to_sql('log', conn, if_exists='append', index=False)

    conn.close()

# Main ------------------

app = FastAPI()

path = './'

openai.api_key = load_file(path + 'api_key.txt')
os.environ['OPENAI_API_KEY'] = openai.api_key

emergency = pd.read_csv(path + 'output_emergency_room.csv')

map_key = load_file(path + 'map_key.txt')
map_key = json.loads(map_key)
c_id, c_key = map_key['c_id'], map_key['c_key']

save_directory = path + "fine_tuned_bert"
model = AutoModelForSequenceClassification.from_pretrained(save_directory)
tokenizer = AutoTokenizer.from_pretrained(save_directory)

@app.get("/")
def read_root():
    return {"Hello": "World"}


# input_text, latitude, longitude를 input으로 받아 가장 가까운 병원 3곳을 추천하는 API
@app.get("/hospital_by_module")
def hospital_by_module(
    request: str = Query(..., description="환자의 상태"),
    latitude: Union[float, None] = Query(None, description="현재 위도"),
    longitude: Union[float, None] = Query(None, description="현재 경도"),
    address: Union[str, None] = Query(None, description="도로명 주소"),
    number_of_hospitals: int = Query(3, description="추천 병원 개수"),
):
    # 위도/경도 또는 도로명 주소 중 하나만 입력받도록 조건 확인
    if (latitude is None or longitude is None) and not address:
        raise HTTPException(status_code=400, detail="위도/경도 또는 도로명 주소 중 하나를 입력해야 합니다.")
    if address and (latitude is not None or longitude is not None):
        raise HTTPException(status_code=400, detail="위도/경도와 도로명 주소를 동시에 입력할 수 없습니다.")
    if address:
        latitude, longitude = get_coordinates_from_address(address, c_id, c_key)
    
    predicted_class, _ = predict(request, model, tokenizer)
    if predicted_class <= 3:
        result = recommend_hospital3(latitude, longitude, emergency, c_id, c_key, number_of_hospitals)
        
        db_path = path + 'db/em.db'
        # insert_db(input_text, predicted_class, latitude, longitude, db_path, result[:3])
        
        # return f"예측 위험 등급: {predicted_class}", f"요약문: {request}", result
        return result
    else:
        # return predicted_class, request, "건강 조심하세요!"
        return

        
@app.get("/get_coordinates")
def get_coordinates(address: str):
    print(f"수신한 주소: {address}")  # 로그 출력
    try:
        lat, lon = get_coordinates_from_address(address, c_id, c_key)
        return {"latitude": lat, "longitude": lon}
    except HTTPException as e:
        print(f"에러 발생: {e.detail}")  # 에러 로그
        raise e


# input_audio는 웹에서 직접 업로드
# GET이 아닌 POST 방식으로 변경
# UploadFile 쓰려면 pip install python-multipart 필요
@app.post("/hospital_by_module_audio")
async def hospital_by_module(
    latitude: Union[float, None] = Query(None, description="현재 위도"),
    longitude: Union[float, None] = Query(None, description="현재 경도"),
    address: Union[str, None] = Query(None, description="도로명 주소"),
    number_of_hospitals: int = Query(..., description="추천 병원 개수"),
    audio: UploadFile = File(...)
):
    # 위도/경도 또는 도로명 주소 중 하나만 입력받도록 조건 확인
    if (latitude is None or longitude is None) and not address:
        raise HTTPException(status_code=400, detail="위도/경도 또는 도로명 주소 중 하나를 입력해야 합니다.")
    if address and (latitude is not None or longitude is not None):
        raise HTTPException(status_code=400, detail="위도/경도와 도로명 주소를 동시에 입력할 수 없습니다.")
    if address:
        latitude, longitude = get_coordinates_from_address(address, c_id, c_key)
    
    temp_file = audio.filename
    with open(temp_file, "wb") as buffer:
        shutil.copyfileobj(audio.file, buffer)
    
    summary_text = audio_to_text('./', audio.filename)
    result_text = text_summary(summary_text).split('"')[3]
    predicted_class, _ = predict(result_text, model, tokenizer)
      
    # 임시 파일 삭제
    os.remove(temp_file)
    
    if predicted_class <= 3:
        result = recommend_hospital3(latitude, longitude, emergency, c_id, c_key, number_of_hospitals)
        
        db_path = path + 'db/em.db'
        # insert_db(result_text, predicted_class, latitude, longitude, db_path, result[:3])
        
        # return f"예측 위험 등급: {predicted_class}", f"요약문: {result_text}", result
        return result
    else:
        # return predicted_class, result_text, "건강 조심하세요!"
        return
    
    
@app.post("/audio_to_text")
async def audio_to_text_endpoint(audio: UploadFile = File(...)):
    try:
        # 음성 파일을 임시로 저장
        temp_file = audio.filename
        with open(temp_file, "wb") as buffer:
            shutil.copyfileobj(audio.file, buffer)
        
        # Whisper로 음성 파일을 텍스트로 변환
        summary_text = audio_to_text('./', audio.filename)
        
        # 텍스트 요약 처리
        result_text = text_summary(summary_text)

        return result_text
    except Exception as e:
        return str(e)