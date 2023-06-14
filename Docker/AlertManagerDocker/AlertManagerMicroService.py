import streamlit as st
from kafka import KafkaConsumer

def transformReport(report):
    return report.replace("List", "")

KAFKA_BOOTSTRAP_SERVERS = 'kafka:9092'
KAFKA_TOPIC_NAME = 'alerts'

consumer = KafkaConsumer(
    KAFKA_TOPIC_NAME,
    bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
    auto_offset_reset="latest", # "earliest",
    value_deserializer=lambda m: m.decode('utf-8')
)

def main():
    st.title('PeaceWatchers Alerts')
    st.markdown("***")
    messageCountWidget = st.sidebar.empty()


    messageCount = 0
    for message in consumer:
        messageCount += 1
        messageCountWidget.markdown(f"Alerts number : **{messageCount}**")

        messageId = str(message.offset)
        messageValue = message.value
        st.markdown("***")
        with st.expander(f"Alert message : {messageId}", expanded=True):
            st.warning(transformReport(messageValue), icon='⚠️')



if __name__ == '__main__':
    main()
