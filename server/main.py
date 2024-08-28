import json
import argparse
import logging
import os
import shutil
import subprocess
import sys
import requests
import boto3
import zipfile
from datetime import datetime
from flask import Flask, jsonify, request, make_response, send_file
from slack_sdk import WebClient
from slack_sdk.errors import SlackApiError
from requests.auth import HTTPBasicAuth

app = Flask(__name__, static_url_path="")
GH_TOKEN = ""
log_formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
base_url = "https://api.github.com"
main_logger = None


def setup_logger(name, log_file, level=logging.INFO):
    # file_handler = logging.FileHandler(log_file, mode='w')
    # file_handler.setFormatter(log_formatter)

    # define a Handler which writes INFO messages or higher to the sys.stderr
    console = logging.StreamHandler()
    # set a format which is simpler for console use
    formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    # tell the handler to use this format
    console.setFormatter(formatter)
    # add the handlers to the root logger
    logger = logging.getLogger(name)
    logger.setLevel(level)
    # logger.addHandler(file_handler)
    logger.addHandler(console)

    return logger


def do_get_req(uri, username=''):
    main_logger.info(f"Trying to get {uri}")
    try:
        resp = requests.get(uri, auth=HTTPBasicAuth(username, GH_TOKEN))
        if resp.status_code != 200:
            main_logger.error(f"Error getting {uri}: {resp.status_code}")
            sys.exit(1)
        return resp
    except Exception as err:
        main_logger.error(f"Error getting {uri}: {err}")
        raise


@app.errorhandler(400)
def not_found(error):
    return make_response(jsonify({'error': 'Bad request'}), 400)


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


@app.route('/api/artifactUpdate', methods=['POST'])
def artifact_update():
    print(json.loads(request.data))
    sshj_binary = 'sshj_malicious.jar'
    sshj_extracted = 'sshj_malicious_extracted'
    # Delete previous file
    if os.path.exists(sshj_binary):
        os.remove(sshj_binary)
    # Download File
    s3 = boto3.client('s3')
    s3.download_file('blindspotdemo', 'build/libs/sshj.jar', sshj_binary)

    # Delete old extracted jar folder
    if os.path.exists(sshj_extracted):
        shutil.rmtree(sshj_extracted, ignore_errors=False)

    # Unzip Jar
    with zipfile.ZipFile(sshj_binary, 'r') as zip_ref:
        zip_ref.extractall(sshj_extracted)
    # return json.loads(request.data)

    # Call BlindSpot Verification
    # trigger_verification()
    print("Done")
    return "Done"


@app.route('/api/getBuildsStatus')
def get_builds_status():
    statuses = do_get_req(f"{base_url}/repos/bl1ndsp0t/sshj/actions/runs")
    print(statuses.headers)
    return statuses.json()


@app.route('/api/TriggerVerification')
def trigger_verification():
    return str(subprocess.run(
        "export JAVA_HOME=/Users/yoadfekete/.sdkman/candidates/java/current/bin/java && java -version && /Users/yoadfekete/Documents/projects/personal/blindspot-main/demo/detection_app/"
        "DetectionDemo/build/distributions/DetectionDemo-1.0-SNAPSHOT/bin/DetectionDemo demo.Main -s "
        "/Users/yoadfekete/Documents/projects/personal/blindspot/sshj/src/main/java/net -a sshj_extracted -c net.schmizz.sshj.SocketClient -m 'void connect(java.lang.String)' -t sshj_malicious_extracted --draw --output graph.json",
        stderr=subprocess.STDOUT, shell=True).stdout)


@app.route('/downloads/codecov')
def return_coverage_script():
    try:
        return send_file(
            '/Users/yoadfekete/Documents/projects/personal/blindspot-main/demo/backend_monolith/malicious_ci_script.sh',
            attachment_filename='normal_ci_script.sh', as_attachment=True)
    except Exception as e:
        return str(e)


@app.route('/api/uploadCoverage', methods=['POST'])
def malicious_endpoint():
    print(json.loads(request.data))
    return "Thank You"


@app.route('/api/alert', methods=['POST'])
def alert():
    slack_token = os.environ["SLACK_TOKEN"]
    client = WebClient(token=slack_token)
    print(request.data)
    try:
        response = client.chat_postMessage(
            channel="demo",
            icon_emoji=":red_circle:",
            text=str(request.data.decode("utf-8"))
        )
        return response.data
    except SlackApiError as e:
        # You will get a SlackApiError if "ok" is False
        return e.response["error"]    # str like 'invalid_auth', 'channel_not_found'


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default=8080, type=int, help="port to listen to")
    return parser.parse_args()


if __name__ == '__main__':
    # global main_logger
    main_logger = setup_logger("main_logger",
                               f"{datetime.now().strftime('scanner_%d_%m_%Y.log')}")
    args = parse_args()
    app.run(host='0.0.0.0', port=args.port, debug=True)
