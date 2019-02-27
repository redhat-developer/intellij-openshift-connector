#!/usr/bin/env groovy

node('rhel7'){
	stage('Checkout repo') {
		deleteDir()
		git url: 'https://github.com/redhat-developer/intellij-openshift-connector',
			branch: "${sha1}"
	}

	def props = readProperties file: 'gradle.properties'
	def isSnapshot = props['projectVersion'].contains('-SNAPSHOT')
	def version = isSnapshot?props['projectVersion'].replace('-SNAPSHOT', ".${env.BUILD_NUMBER}"):props['projectVersion'] + ".${env.BUILD_NUMBER}"

	stage('Build') {
		sh "./gradlew assemble"
	}

	stage('Package') {
        sh "./gradlew buildPlugin -PprojectVersion=${version}"
	}

	if(params.UPLOAD_LOCATION) {
		stage('Upload') {
			def filesToPush = findFiles(glob: '**/*.zip')
			sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${filesToPush[0].path} ${UPLOAD_LOCATION}/snapshots/intellij-openshift-connector/"
            stash name:'zip', includes:filesToPush[0].path
		}
    }

    if(publishToMarketPlace.equals('true')){
        if (isSnapshot) {
            error("Check the code, version is SNAPSHOT")
        } else {
        	timeout(time:5, unit:'DAYS') {
        		input message:'Approve deployment?', submitter: 'jmaury'
    	    }

    	    stage("Publish to Marketplace") {
                unstash 'zip'
                withCredentials([[$class: 'usernamePassword', credentialsId: 'JetBrains marketplace token', usernameVariable: 'USERNAME', passwordVariable: 'USERPWD']]) {
                    sh './gradlew publishPlugin -PjetBrainsUsername=${USERNAME} -PjetBrainsPassword=${USERPWD}'
                }
                archive includes:"**.zip"

                stage("Promote the build to stable") {
                    def zip = findFiles(glob: '**/*.zip')
                    sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${zip[0].path} ${UPLOAD_LOCATION}/stable/intellij-openshift-connector/"
                }
            }
        }
    }
}
