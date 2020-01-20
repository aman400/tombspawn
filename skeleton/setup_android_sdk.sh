#!/bin/bash
# set -e

DEBUG=0
VERSION=1.0.0
# SDK_INSTALLATION = -1 = None
# SDK_INSTALLATION = 0 = Basic
# SDK_INSTALLATION = 1 = Full

# SDK_UPDATE = 0 = Force Installtion if exists and not --none
# SDK_UPDATE = 1 = Update if SDK exists

# NO_JAVA = 0 = With Java Installation
# NO_JAVA = 1 = Skip Java Installation

# NO_APT = 0 = With APT Update
# NO_APT = 1 = Skip APT Update

SDK_INSTALLATION=0
SDK_UPDATE=1
NO_JAVA=0
NO_APT=0
ANDROID_SDK_EXISTS=0
[[ -d "$ANDROID_SDK_DIR" ]] && ANDROID_SDK_EXISTS=1

ANDROID_SDK_DIR=$HOME/android-sdk
ANDROID_LINUX_SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip"

fullPackages=("add-ons;addon-google_apis-google-15"
  "add-ons;addon-google_apis-google-16"
  "add-ons;addon-google_apis-google-17"
  "add-ons;addon-google_apis-google-18"
  "add-ons;addon-google_apis-google-19"
  "add-ons;addon-google_apis-google-21"
  "add-ons;addon-google_apis-google-22"
  "add-ons;addon-google_apis-google-23"
  "add-ons;addon-google_apis-google-24"
  "add-ons;addon-google_gdk-google-19"
  "build-tools;19.1.0"
  "build-tools;20.0.0"
  "build-tools;21.1.2"
  "build-tools;22.0.1"
  "build-tools;23.0.1"
  "build-tools;23.0.2"
  "build-tools;23.0.3"
  "build-tools;24.0.0"
  "build-tools;24.0.1"
  "build-tools;24.0.2"
  "build-tools;24.0.3"
  "build-tools;25.0.0"
  "build-tools;25.0.1"
  "build-tools;25.0.2"
  "build-tools;25.0.3"
  "build-tools;26.0.0"
  "build-tools;26.0.1"
  "build-tools;26.0.2"
  "build-tools;26.0.3"
  "build-tools;27.0.0"
  "build-tools;27.0.1"
  "build-tools;27.0.2"
  "build-tools;27.0.3"
  "build-tools;28.0.0"
  "build-tools;28.0.1"
  "build-tools;28.0.2"
  "build-tools;28.0.3"
  "build-tools;29.0.0"
  "build-tools;29.0.1"
  "build-tools;29.0.2"
  "cmake;3.6.3155560"
  "extras;android;gapid;1"
  "extras;android;gapid;3"
  "extras;android;m2repository"
  "extras;google;auto"
  "extras;google;google_play_services"
  "extras;google;instantapps"
  "extras;google;m2repository"
  "extras;google;market_apk_expansion"
  "extras;google;market_licensing"
  "extras;google;play_billing"
  "extras;google;simulators"
  "extras;google;webdriver"
  "lldb;2.0"
  "lldb;2.1"
  "lldb;2.2"
  "lldb;2.3"
  "patcher;v4"
  "platforms;android-21"
  "platforms;android-22"
  "platforms;android-23"
  "platforms;android-24"
  "platforms;android-25"
  "platforms;android-26"
  "platforms;android-27"
  "platforms;android-28"
  "platforms;android-29"
  )

basicPackages=(
  "tools"
  "build-tools;29.0.2"
  "build-tools;28.0.3"
  "platform-tools"
  "platforms;android-28"
  "extras;google;google_play_services"
  "extras;google;m2repository"
  "extras;android;m2repository"
  "extras;android;gapid;1"
  "extras;android;gapid;3"
  "extras;google;instantapps"
  "extras;google;webdriver"
  "extras;m2repository;com;android;support;constraint;constraint-layout-solver;1.0.2"
  "extras;m2repository;com;android;support;constraint;constraint-layout;1.0.2"
  "patcher;v4")

function printAndSleep() {
  ((counter++))
  echo "
	###### $1 ######
	"
  sleep 1
}

# Prints script version
function printVersion() {
  echo "
Android SDK Setup for Linux
Version $VERSION
"
  exit 0
}

# Updates machine APT
function updateApt() {
  printAndSleep "Updating apt"
  apt-get -y update
}

# Installs Java-8 (OpenJdk-8)
function installJava() {
  JAVA_VER=$(java -version 2>&1 | sed -n ';s/.* version "\(.*\)\.\(.*\)\..*"/\1\2/p;')
  if [ "$JAVA_VER" -ge 18 ]; then
    printAndSleep "Java $JAVA_VER Found"
  else
    printAndSleep "Installing OpenJdk 8"
    apt-get -y install openjdk-8-jdk
  fi
}

#Installs unzip tool
function installUnzip() {
  if ! type "unzip" &>/dev/null; then
    printAndSleep "Installing Unzip tool"
    apt-get -y install unzip
  fi
}

function updateAndroidHomeVar() {
  printAndSleep "Adding ANDROID_HOME to environmental variables"

  echo "" >>~/.bashrc
  echo "export ANDROID_HOME=\"$ANDROID_SDK_DIR\"" >>~/.bashrc
  echo "" >>~/.bashrc
  echo "export PATH=\"${PATH}:${ANDROID_SDK_DIR}/tools/:${ANDROID_SDK_DIR}/platform-tools/\"" >>~/.bashrc
}

#Accept Android SDK Licenses
function acceptAndroidSdkLicenses() {
  # navigate into our directory
  cd "$ANDROID_SDK_DIR" || exit

  printAndSleep "Accepting Android SDK licenses"
  mkdir -p licenses/
  echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55" >"licenses/android-sdk-license"
  echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" >"licenses/android-sdk-preview-license"
  echo -e "\n152e8995e4332c0dc80bc63bf01fe3bbccb0804a
d975f751698a77b662f1254ddbeed3901e976f5a" >"licenses/intel-android-extra-license"

  cd "$ANDROID_SDK_DIR"/tools/bin || exit
  yes | ./sdkmanager --licenses
}

# Updates already installed Android SDK packages
function updateAndroidSdkPackages() {
  printAndSleep "Checking for Android SdkManager updates..."

  cd "$ANDROID_SDK_DIR"/tools/bin || exit
  ./sdkmanager --update
}

# Updates installed Android SDK
function updateAndroidSdk() {
  [ $ANDROID_SDK_EXISTS == -1 ] && echo "Android SDK not found, please install --basic or --full" && exit 1
  printAndSleep "Updating Android SDK"

  acceptAndroidSdkLicenses
  updateAndroidSdkPackages
}

#Downloads Android Linux SDK tools to the $home directory
function downloadLinuxAndroidSdk() {
  printAndSleep "Downloading linux android sdk tools"
  wget -nc -O $HOME/sdk-tools-linux.zip $ANDROID_LINUX_SDK_URL
}

#1- Create sdk directory
#2- Unzip android linux sdk tools
function unzipAndroidLinuxSdk() {
  mkdir -p $ANDROID_SDK_DIR

  printAndSleep "unzipping linux android sdk tools"
  unzip -o $HOME/sdk-tools-linux.zip -d $ANDROID_SDK_DIR
}

function installAndroidSdkPackages() {
  printAndSleep "Installing Android SDK Packages"
  packages=("$@")
  cd "$ANDROID_SDK_DIR"/tools/bin || exit
  for package in "${packages[@]}"; do
    printAndSleep "Installing $package"
    ./sdkmanager "$package"
  done
}

function addMissingRepositoriesFile() {
  [ ! -f $HOME/.android/repositories.cfg ] && echo "### User Sources for Android SDK Manager" >>$HOME/.android/repositories.cfg && echo "count=0" >>$HOME/.android/repositories.cfg
}

function installAndroidSdk() {
  installUnzip
  if [ $ANDROID_SDK_EXISTS == 0 ]; then
    downloadLinuxAndroidSdk
    unzipAndroidLinuxSdk
    updateAndroidHomeVar
  fi
  acceptAndroidSdkLicenses
  addMissingRepositoriesFile

  packages=("$@")
  installAndroidSdkPackages "${packages[@]}"

  du -hs "$ANDROID_SDK_DIR"
}

# Install basic most common android sdk packages
function installBasicAndroidSdk() {
  printAndSleep "Installing Basic Android SDK"
  installAndroidSdk "${basicPackages[@]}"
}

# Install basic full android sdk packages
function installFullAndroidSdk() {
  printAndSleep "Installing Full Android SDK"
  installAndroidSdk "${fullPackages[@]}"
}

while [[ $# -gt 0 ]]; do
  key="$1"

  case $key in
  -n | --none)
    SDK_INSTALLATION=-1
    ;;
  -b | --basic)
    SDK_INSTALLATION=0
    ;;
  -f | --full)
    SDK_INSTALLATION=1
    ;;
  -u | --update)
    SDK_UPDATE=1
    ;;
  -j | --no-java)
    NO_JAVA=1
    ;;
  -a | --no-apt)
    NO_APT=1
    ;;
  -v | --version)
    printVersion
    ;;
  *) ;; # unknown option
  esac
  shift # past argument or value
done

if [ $DEBUG == 1 ]; then
  SDK_INSTALLATION=1
  SDK_UPDATE=1
  NO_JAVA=0
  NO_APT=0
fi

echo SDK_INSTALLATION = "${SDK_INSTALLATION}"
echo NO_JAVA = "${NO_JAVA}"
echo NO_APT = "${NO_APT}"
echo ANDROID_SDK_EXISTS = "${ANDROID_SDK_EXISTS}"
echo SDK_UPDATE = "${SDK_UPDATE}"

[ $NO_APT == 0 ] && updateApt
[ $NO_JAVA == 0 ] && installJava

if [ $ANDROID_SDK_EXISTS == 1 ] && [ $SDK_UPDATE == 1 ] && [ $SDK_INSTALLATION -gt -1 ]; then
  updateAndroidSdk
elif [ $SDK_UPDATE == 1 ] && [ $SDK_INSTALLATION == -1 ]; then
  updateAndroidSdk
else
  if [ $SDK_INSTALLATION == -1 ]; then
    printAndSleep "Skipping Android SDK Installation"
  elif [ $SDK_INSTALLATION == 0 ]; then
    installBasicAndroidSdk
    updateAndroidSdk
  elif [ $SDK_INSTALLATION == 1 ]; then
    installFullAndroidSdk
    updateAndroidSdk
  fi
fi
