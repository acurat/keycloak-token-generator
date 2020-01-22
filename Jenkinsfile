#!groovy

def preMerge() {
    gitlabBuilds(builds: ['Maven Verify']) {
        stage('Maven Verify') {
            gitlabCommitStatus(name: 'Maven Verify') {
                sh "${constants.mvnCmd} verify"
            }
        }
    }
}

def merge() {

    // Build
    stage('Maven Verify') {
        sh "${constants.mvnCmd} install"
    }

    stage('Deploy DEV') {
        buildAndInstallJar()
    }
}

jdk11Template(this.&preMerge, this.&merge)
