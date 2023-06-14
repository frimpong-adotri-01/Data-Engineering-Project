FROM python:3.11

WORKDIR /app

COPY DataVizMicroService.py /app
COPY requirements.txt /app
COPY peace-logo.png /app

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8502
CMD ["streamlit", "run", "--server.port", "8502", "DataVizMicroService.py"]