// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**

  As long as the BO UI view doesn't show the downstream URL in the view let's use
  the suggested snippet from https://issues.jenkins-ci.org/browse/JENKINS-38339

  Further details: https://brokenco.de/2017/08/03/overriding-builtin-steps-pipeline.html

  build(job: 'foo', parameters: [string(name: "my param", value: some_value)])
*/
def call(Map params = [:]){
  def job = params.job
  def parameters = params.parameters
  def wait = params.get('wait', true)
  def propagate = params.get('propagate', true)
  def quietPeriod = params.get('quietPeriod', 1)

  def buildInfo
  try {
      buildInfo = steps.build(job: job, parameters: parameters, wait: wait, propagate: propagate, quietPeriod: quietPeriod)
  } catch (Exception e) {
      log(level: 'INFO', text: "${getRedirectLink(e, job)}")
      throw e
  }
  log(level: 'INFO', text: "${getRedirectLink(buildInfo, job)}")
  return buildInfo
}

def getRedirectLink(buildInfo, jobName) {
  if(buildInfo instanceof Exception) {
    def buildNumber = ''
    buildInfo.toString().split(" ").each {
      if(it.contains("#")) {
        buildNumber = it.substring(1)
      }
    }
    if (buildNumber.trim()) {
      return "For detailed information see: ${env.JENKINS_URL}job/${jobName.replaceAll('/', '/job/')}/${buildNumber}/display/redirect"
    } else {
      return "Can not determine redirect link!!!"
    }
  } else if(buildInfo instanceof org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper) {
    return "For detailed information see: ${buildInfo.getAbsoluteUrl()}display/redirect"
  } else {
    return "Can not determine redirect link!!!"
  }
}
