"""This module releases local built jars to staging repo."""

import json
import utilities
import sys

def main():
    """ External json configuration can be passed as argument """
    external_config = {}
    if len(sys.argv) > 1:
        external_config = json.loads(sys.argv[1])

    """
    Release jars to staging repo
    """
    with open('config.json') as config_file:
        configs = json.load(config_file)

    replace_config(configs, external_config)

    jar_list = utilities.upload_jars(configs)
    utilities.sign_jars(configs)

    artifact_folder = utilities.prepare_artifacts(configs, jar_list)

    repo_id = utilities.create_staging_repo(configs)
    utilities.deploy_to_staging_repo(configs, artifact_folder, repo_id)
    utilities.close_staging_repo(configs, repo_id)

def replace_config(file_config, external_config):
    """ If external config not empty, replace config in json with external configuration """
    if "moduleNames" in external_config:
        file_config["moduleNames"] = external_config["moduleNames"]

    if "releaseVersion" in external_config:
        file_config["releaseVersion"] = external_config["releaseVersion"]

    if "passwords" in external_config:
        password_config = external_config["passwords"]
        if "jenkins" in password_config:
            file_config["passwords"]["jenkins"] = password_config["jenkins"]
        if "gpg" in password_config:
            file_config["passwords"]["gpg"] = password_config["gpg"]
        if "nexus" in password_config:
            file_config["passwords"]["nexus"] = password_config["nexus"]

if __name__ == "__main__":
    main()
