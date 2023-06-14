# ============================== import ==============================
import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import plotly.express as px
import plotly.graph_objects as go
from streamlit_lottie import st_lottie
import requests
from plotly.subplots import make_subplots
import os
import boto3
from wordcloud import WordCloud
import itertools


# =============================== S3 BUCKET SETUP ===============================

# Spécifier les informations d'authentification à l'aide de variables d'environnement
os.environ['AWS_ACCESS_KEY_ID'] = 'AKIAXYZJEEAPINFY5RLO'
os.environ['AWS_SECRET_ACCESS_KEY'] = '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'

s3 = boto3.client('s3')

bucket_name = 'peacestate'

prefix = 'donneePeaceWatcher.json/part'
objects = s3.list_objects_v2(Bucket=bucket_name, Prefix=prefix)

# Liste des noms de tous les fichiers dans le bucket S3
file_names = [obj['Key'] for obj in objects['Contents']]
print("====================================================================")
#print(file_names)

# Concaténer le contenu de tous les fichiers
concatenated_content = ''
for file_name in file_names:
    response = s3.get_object(Bucket=bucket_name, Key=file_name)
    file_content = response['Body'].read().decode('utf-8')
    concatenated_content += file_content

# Afficher le contenu concaténé de tous les fichiers
#print(concatenated_content)

# ===================================================================================




# =============================== STREAMLIT APP SETUP ===============================

# Config
st.set_page_config(
    layout="wide",
    page_title="PeaceState Visualization App",
    page_icon="🕊️"
)

df = pd.read_json(concatenated_content, lines=True)

#streamlit part
def load_lottieurl(url: str):
    r = requests.get(url)
    if r.status_code != 200:
        return None
    return r.json()

def extracthour(dt) :
    return dt.hour

def extractmonth(dt) :
    return dt.month

def extractday(dt) :
    return dt.day

st.sidebar.image("peace-logo.png")
st.sidebar.header("**Peace State**")
st.sidebar.markdown("***")
st.title('Data visualization : Project PeaceState')


#Function to display the homepage of the application
def home():
    st.markdown("---")
    st.markdown("### <span style=\"color:red\">KPIs</span>", unsafe_allow_html=True)
    nbAlert  = df["names"].apply(lambda x: sum(score < 0 for score in x.values())).sum()
    nbNoAlert = df["names"].apply(lambda x: sum(score >= 0 for score in x.values())).sum()
    alerts, no_alerts = st.columns(2)
    alerts.metric(label='Number of Alerts  🔔', value=nbAlert, delta_color='off')
    #st.markdown(f"<font color='red'>{nbAlert}</font>", unsafe_allow_html=True)
    no_alerts.metric(label='Number of No Alerts  🔕', value=nbNoAlert, delta_color='off')
    #st.markdown(f"<font color='green'>{nbNoAlert}</font>", unsafe_allow_html=True)
    st.markdown("---")
    st_lottie(load_lottieurl("https://assets6.lottiefiles.com/packages/lf20_49rdyysj.json"))

#Function to display the head of the dataset
def data_header():
    st.header('Header of Dataframe')
    st.dataframe(df.head(10), 1000, 400)


#Function to display the mapBox of Alerts
def mapboxAlerts():
    dfAlert = df[df['names'].apply(lambda x: any(score < 0 for score in x.values()))]
    
    fig = px.scatter_mapbox(dfAlert, lat=dfAlert["currentLocation"].apply(lambda x: x[0]), 
                            lon=dfAlert["currentLocation"].apply(lambda x: x[0]),
                            color_discrete_sequence=["magenta"], zoom=10, height=600).update_traces(marker=dict(size=10))                 
    fig.update_layout(mapbox_style="dark")
    fig.update_layout(mapbox_style="open-street-map", title="Map showing the location of alerts")
    st.plotly_chart(fig)

def heatmap(scores):
    colorscale = [[0, 'red'], [0.5, 'white'], [1, 'green']]
    fig = go.Figure(data=go.Heatmap(z=scores, colorscale=colorscale))
    fig.update_layout(xaxis_title='Peace Score')
    st.plotly_chart(fig)

#Function to display the histogram of PeaceScore
def histogramPeaceScore(scores):
    st.header("Frequency of peace Scores")
    fig = go.Figure(data=go.Histogram(x=scores, nbinsx=50))
    fig.update_layout(xaxis_title='Scores', yaxis_title='Frequency')
    st.plotly_chart(fig)

def mostAgitated():
    dfAlerts = df[df['names'].apply(lambda x: any(score < 0 for score in x.values()))]
    badScores = []
    for x in dfAlerts['names']:
        for name, score in x.items():
            if score < 0:
                badScores.append((name, score))

    badScoresSorted = sorted(badScores, key=lambda x: x[1])[:10] #top 10 most agitated citizens

    badScoresSortedData = [("NAME", "SCORE")] + badScoresSorted

    st.header("Top 10 names with very Bad Peace Scores")
    st.write(pd.DataFrame(badScoresSorted, columns=["name", "score"]))

def statisticByDate() :
    df["nbAlert"] = df["names"].apply(lambda x: sum(score < 0 for score in x.values()))
    df["nbNoAlert"] = df["names"].apply(lambda x: sum(score >= 0 for score in x.values()))
    df['hour'] = df["timestamp"].apply(extracthour)
    df['month'] = df["timestamp"].apply(extractmonth)
    df['day'] = df["timestamp"].apply(extractday)

    st.header("Alert vs Non Alert Group By Month")
    df_month = df[["nbAlert", "nbNoAlert", "month"]].groupby("month").sum()[["nbAlert", "nbNoAlert"]]
    trace1 = go.Bar(x=df_month.index, y=df_month.nbAlert, name="Alert")
    trace2 = go.Bar(x=df_month.index, y=df_month.nbNoAlert, name="Non Alert")
    data = [trace1, trace2]
    fig = go.Figure(data)
    fig.update_layout(barmode='group')
    st.plotly_chart(fig)

    st.markdown("---")
    st.header("Alert vs Non Alert Group By Day")
    df_day = df[["nbAlert", "nbNoAlert", "day"]].groupby("day").sum()[["nbAlert", "nbNoAlert"]]
    trace1 = go.Bar(x=df_day.index, y=df_day.nbAlert, name="Alert")
    trace2 = go.Bar(x=df_day.index, y=df_day.nbNoAlert, name="Non Alert")
    data = [trace1, trace2]
    fig = go.Figure(data)
    fig.update_layout(barmode='group')
    st.plotly_chart(fig)

    st.markdown("---")
    st.header("Alert vs Non Alert Group By hour")
    df_hour = df[["nbAlert", "nbNoAlert", "hour"]].groupby("hour").sum()[["nbAlert", "nbNoAlert"]]
    trace1 = go.Bar(x=df_hour.index, y=df_hour.nbAlert, name="Alert")
    trace2 = go.Bar(x=df_hour.index, y=df_hour.nbNoAlert, name="Non Alert")
    data = [trace1, trace2]
    fig = go.Figure(data)
    fig.update_layout(barmode='group')
    st.plotly_chart(fig)

    
    


def wordCloud() :
    st.markdown("---")
    st.header("Word cloud with keywords on reports")
    flat_list = itertools.chain(df.keywords.values)
    keywords = list(flat_list)
    flat_list = [word for sublist in keywords for word in sublist]
    text = ' '.join(flat_list)

    # Créer un objet WordCloud
    wordcloud = WordCloud(width=800, height=400, background_color='white').generate(text)

    # Afficher le nuage de mots
    fig = plt.figure(figsize=(20, 10))
    plt.imshow(wordcloud, interpolation='bilinear')
    plt.axis("off")
    plt.show()
    st.pyplot(fig)

    st.markdown("---")
    st.header("Word Occurence")

    wordcount = pd.Series(flat_list).value_counts()
    fig = px.bar(x=wordcount.index, y=wordcount.values,
                    labels={
                        "x": "word",
                        "y": "occurence",
                        
                    },width=1000, height=600)
    st.plotly_chart(fig)



#Sidebar navigation
st.sidebar.title('Navigation')
options = st.sidebar.radio('Select what you want to display:', ['Home', 'Data Header', 'Location Alerts', 'Peace Score','Statistics by date','Word Occurence'])

# Navigation options
if options == 'Home':
    st.markdown("---")
    home()
elif options == 'Data Header':
    st.markdown("---")
    data_header()
elif options == 'Location Alerts':
    st.markdown("---")
    mapboxAlerts()
elif options == 'Peace Score':
    st.markdown("---")
    scores = [score for row in df['names'] for score in row.values()]
    #heatmap(scores)
    histogramPeaceScore(scores)
    mostAgitated()
elif options == 'Statistics by date' :
    st.markdown("---")
    statisticByDate()
elif options == 'Word Occurence' :
    st.markdown("---")
    wordCloud()

