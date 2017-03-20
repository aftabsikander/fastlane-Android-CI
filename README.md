[![Build Status](https://travis-ci.org/aftabsikander/fastlane-Android-CI.svg?branch=master)](https://travis-ci.org/aftabsikander/fastlane-Android-CI)
# Introduction
Creating Android App always involves tracking its [Version Code](#version-code) and [Version Name](#version-name) across different [Product Flavors](#product-flavors) and increment these values when shipping new feature into the market this process may be tedious and error prone. This project shows how we can automate this process with the combination of gradle and Fastlane. 

# Terminology

## Version Code

 An integer used as an internal version number. This number is used only to determine whether one version is more recent than another, with higher numbers indicating more recent versions. This is not the version number shown to users; that number is set by the versionName setting, below.
 
 The value is an integer so that other apps can programmatically evaluate it, for example to check an upgrade or downgrade relationship. we can set the value to any integer we want, however we should make sure that each successive release of our app uses a greater value. The system does not enforce this behavior, but increasing the value with successive releases is normative.
 
 Typically, we would release the first version of our app with versionCode set to 1, then monotonically increase the value with each release, regardless whether the release constitutes a major or minor release. This means that the versionCode value does not necessarily have a strong resemblance to the app release version that is visible to the user (see [versionName](#version-name), below). 
 
 **Apps and publishing services should not display this version value to users**.

> Warning: The greatest value Google Play allows for versionCode is
> 2100000000.

## Version Name

 A string used as the version number shown to users. This setting can be specified as a raw string or as a reference to a string resource.
The value is a string so that you can describe the app version as a &lt;major&gt;.&lt;minor&gt;.&lt;patch&gt;. string, or as any other type of absolute or relative version identifier. The versionName has no purpose other than to be displayed to users.

For our application we have used versionName as below

    <major>.<minor>.<patch>(buildNumber)
    1.0.0(14)

## Product Flavors

A product flavor defines a customized version of the application build by the project. A single project can have different flavors which change the generated application.

# Prerequisites

Create two version properties files for each Product Flavors. i.e (one for successful incremental task and other for handling error case which will revert all update made to version name and version code.

Update file path in `project.ext` as shown in root [build.gradle](build.gradle) file


>`VERSION_CODE` and `VERSION_MAJOR` should start from 1 by default in success and error version property file. 

**Example**

**DevelopmentVersion.properties**
```
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=1
VERSION_BUILD=0
```
**DevelopmentErrorVersion.properties**
```
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=1
VERSION_BUILD=0
```

## How Version Incremental Process Works

`performVersionCodeAndVersionNumberIncrement` gradle task is responsible for version name and version code incremental. It takes two command line parameters i.e. [Version Type](#version-type-parameter) & [Build Variant Type](#build-variant-type-parameter). 

### Build Variant Type Parameter

This parameter tells which product flavor we need to use for incremental task i.e `(Production,Stagging,Qa,Developement)`. In majority of our cases we have up to 4 product flavors or less. For demonstration I have used only three flavors i.e. `(Production,Qa,Development`) and these flavors are defined as constant values in [build.gradle](build.gradle). Its command line parameter key name is `buildVariantType`

    productionFlavor="production"
    qaFlavor="qa"
    developmentFlavor="development"

These constant values are used to validate user input given from command line they are case sensitive. 

### Version Type Parameter

This parameter tells which version to increment i.e `(Major,Minor,Patch,Build)`. Its command line parameter key name is `versionType`

#### Example
```
gradlew task -PversionType=development -PbuildVariantType=Build performVersionCodeAndVersionNumberIncrement
```
Above code snippet will increment development flavor version code only as shown below.


**DevelopmentVersion.properties**
```
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=2
VERSION_BUILD=1
```
**DevelopmentErrorVersion.properties**
```
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=1
VERSION_BUILD=0
```
**Important** 

As you can see `DevelopmentVersion.properties`  file contains `DevelopmentErrorVersion.properties`  previous incremental values, we are storing these values as backup if any error occurs during broken build. Later on we can [revert](#revert-incremental) these changes.
### Revert Incremental Changes

In case we encounter any error during incremental task or external errors we need to revert changes which are made in version properties files using `revertVersionCodeForVariant` gradle task. It takes single command line parameter i.e. `buildVariantType`

 **Example**
```
gradlew -PbuildVariantType=development revertVersionCodeForVariant
```
Above code snippet will revert Incremental changes made in development flavor version properties file 

# Using Fastlane

Fastlane provides huge collection of tools and scripts which we can use to automate our respective daily task. 

How to setup Fastlane in Android Project visit fastlane [docs](https://docs.fastlane.tools/getting-started/android/setup/)

## Available Actions

#### Android devVariant
Generate Build for Development Variant and deploy build on [crashlytics beta](https://docs.fabric.io/apple/beta/overview.html)

    fastlane devVariant

#### Android QaVariant
Generate Build for QA Variant and deploy build on [crashlytics beta](https://docs.fabric.io/apple/beta/overview.html)

    fastlane qaVariant


## Fastlane Actions And Configurations

Following are few [Fastlane Actions](https://docs.fastlane.tools/actions/) and different techniques used to create [Development Variant](#android-devVariant) and [Qa Variant](#android-qaVariant) lanes. 

### List Of Fastlane Actions Used
- [Ensure Git Branch](https://docs.fastlane.tools/actions/#ensure_git_branch)
- [Ensure Git Status Clean](https://docs.fastlane.tools/actions/#ensure_git_status_clean)
- [Last Git Commit](https://docs.fastlane.tools/actions/#last_git_commit)
- [Git Commit](https://docs.fastlane.tools/actions/#git_commit)
- [Reset Git Repo](https://docs.fastlane.tools/actions/#reset_git_repo)
- [Push To Git Remote](https://docs.fastlane.tools/actions/#push_to_git_remote)
- [Puts](https://docs.fastlane.tools/actions/#puts)
- [Is CI](https://docs.fastlane.tools/actions/#is_ci)
- [Gradle](https://docs.fastlane.tools/actions/#gradle)
- [Crashlytics](https://docs.fastlane.tools/actions/#crashlytics)

### Setup Environment Variable 
You can define environment variables in a `.env` or `.env.default` file in the same directory as your Fastfile. Environment variables are loading using [dotenv](https://github.com/bkeepers/dotenv). Here's an example.

    WORKSPACE=YourApp.xcworkspace
    HOCKEYAPP_API_TOKEN=your-hockey-api-token

`fastlane` also has a `--env` option that allows loading of environment specific dotenv files. `.env` and `.env.default` will be loaded before environment specific dotenv files are loaded. The naming convention for environment specific dotenv files is .env.<environment>

#### Example
`fastlane <lane-name> --env development` 

Above code snippet will load `.env`, `.env.default`, and `.env.development` 

#### Should I commit my .env file?

Credentials should only be accessible on the machines that need access to them. Never commit sensitive information to a repository that is not needed by every development machine and server.

Personally, I prefer to commit the .env file with development-only settings. This makes it easy for other developers to get started on the project without compromising credentials for other environments. If you follow this advice, make sure that all the credentials for your development environment are different from your other deployments and that the development credentials do not have access to any confidential data.


### How We Automated Version Type Parameter
Approach which we took in automating version type is inspired by [Pivotal Tracker SCM Post-Commit Message Syntax](https://www.pivotaltracker.com/help/api?version=v3#scm_post_commit_message_syntax) we followed the same technique and introduced few keywords which are case sensitive and are listed below.

 - Major
 - Minor
 - Patch
 - Build

**Example**
```
 [Patch] Fixed battery extensive usage.
```

 **Important**

> If multiple `Version Type`  Tag are found in a commit we process only first successful matched tag and ignore rest of the them.

**Example**
```
 [Major] [Build] Fixed battery extensive usage.[Patch]
```
As shown above we found multiple `version type` tags in git commit, we will only extract `[Major]` tag and processed the build with this, rest of the tag will be ignored.

### Push Changes To Remote
When using `git_commit` action to commit changes to remote repository in fastlane setup. If you are using webhooks to trigger build on a push this will cause an infinite loop of triggering builds. 

We need to configure our CI to ignore build triggering which contains `[ci-skip]` tag in commit message. 

#### Example
If you are using Gitlab you will need the [GitLab Plugin](https://wiki.jenkins-ci.org/display/JENKINS/GitLab+Plugin). Inside the job you want to configure, go to  `Build Triggers > Build when a change is pushed to GitLab > Enable [ci-skip]`. When you include `[ci-skip]` in your build this commit won't trigger the build in jenkins at all.

    git_commit(path:"./CHANGELOG.md", message:"[ci-skip] Updated CHANGELOG for Build #{build_number}")
    push_to_git_remote

## Todo

+ [x] Support Multiple Crashlytics accounts
+ [ ]  Inject required credentials from CI environment variable
+ [ ]  Setup Travis-CI/Circle-CI/GitLab-CI
+ [ ]  Support Slack/EmailNotifications



# Built With


* [Fastlane](https://github.com/fastlane/fastlane) - tool for iOS, Mac, and Android developers to automate tasks
* [Gradle](https://gradle.org/) - Dependency Management

# Contributing


Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

# Authors

* [Aftab Ali](https://github.com/aftabsikander)

# License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

# Acknowledgments

* A big thanks to [Fastlane Team ](https://github.com/fastlane/fastlane) for its amazing tool.
* A big thanks to [Kevin Long](https://medium.com/@kmlong1183) for its comprehensive [article](https://medium.com/@kmlong1183/using-fastlane-tools-and-android-2e8d76bb138b#.69ko3fi7k)

