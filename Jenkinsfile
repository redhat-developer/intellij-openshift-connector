#!/usr/bin/env groovy

node('rhel7'){
	stage('Checkout repo') {
		deleteDir()
		git url: 'https://github.com/redhat-developer/intellij-openshift-connector',
			branch: "${BRANCH}"
	}

	stage('Build') {
		sh "./gradlew assemble"
	}

	stage('Package') {
	    def props = readProperties file: 'gradle.properties'
	    def version = props['projectVersion'].replace('SNAPSHOT', '${env.BUILD_NUMBER}')
        sh "./gradlew buildPlugin -PprojectVersion=${version}"
	}

	if(params.UPLOAD_LOCATION) {
		stage('Upload') {
			def filesToPush = findFiles(glob: '**.zip')
			sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${filesToPush[0].path} ${UPLOAD_LOCATION}/snapshots/intellij-openshift-connector/"
            stash name:'zip', includes:filesToPush[0].path
		}
    }
}
