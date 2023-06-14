FROM python:3.11

WORKDIR /app
COPY AlertManagerMicroService.py /app
COPY requirements.txt /app

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8501
CMD ["streamlit", "run", "AlertManagerMicroService.py"]