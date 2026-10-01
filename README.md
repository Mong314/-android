# Phone Assist — 전원 종료 타이머 (1단계)

## PC 없이 폰만으로 빌드하는 방법 (GitHub Actions)
이 프로젝트에는 `.github/workflows/android-build.yml`이 포함되어 있어, GitHub에 올리기만 하면 클라우드에서 자동으로 APK를 빌드해준다.

1. 폰에서 `PhoneAssist.zip` 압축 해제 (파일 관리자 또는 ZArchiver 앱).
2. GitHub 모바일 앱 설치 후 계정 생성/로그인, 빈 저장소(repository) 1개 새로 만들기 (README 없이, Public).
3. Termux 앱 설치 (F-Droid 버전 권장) 후 다음 실행:
   ```
   pkg update && pkg install git openssh -y
   termux-setup-storage
   cd ~/storage/downloads
   cd PhoneAssist   # 압축 푼 폴더로 이동
   git init
   git add .
   git commit -m "init"
   git branch -M main
   git remote add origin https://<사용자명>:<토큰>@github.com/<사용자명>/<저장소명>.git
   git push -u origin main
   ```
   `<토큰>`은 GitHub 웹(모바일 브라우저) > 오른쪽 위 프로필 > Settings > Developer settings > Personal access tokens에서 `repo` 권한으로 생성한 값.
4. 저장소의 "Actions" 탭으로 이동 → 자동 실행된 빌드 확인 (첫 실행은 워크플로 허용 버튼을 한 번 눌러야 할 수 있음, 5~10분 소요).
5. 빌드 완료 후 해당 실행(run) 페이지 하단 "Artifacts"에서 `app-debug-apk` 다운로드 (zip 형태로 폰에 저장됨).
6. 다운로드한 zip 안의 `.apk` 파일을 파일 관리자에서 추출 후 탭하여 설치.
   - 최초 설치 시 "출처를 알 수 없는 앱" 설치 허용 필요 (설정에서 해당 브라우저/파일관리자 앱에 권한 부여).

## PC가 있는 경우: Android Studio로 빌드
1. 이 폴더를 Android Studio에서 "Open" 으로 연다.
2. Gradle 동기화가 자동으로 진행된다 (Gradle Wrapper 파일이 없으면 Android Studio가 자동 생성/다운로드함, 인터넷 연결 필요).
3. USB 디버깅이 켜진 폰을 연결하거나 APK를 빌드해 설치한다 (`Build > Build APK(s)`).

## 실제 동작 조건
- **루트(root)가 있는 기기**: 앱이 `su`를 통해 `reboot -p`를 실행해 실제로 전원을 끈다. 최초 실행 시 Magisk 등에서 이 앱에 su 권한을 허용해야 한다.
- **루트가 없는 일반 기기(대부분의 폰)**: OS 정책상 서드파티 앱은 전원을 끌 수 없다. 이 경우 앱은 자동으로 "화면 잠금"으로 대체 동작한다. 이를 위해 앱 실행 후 "화면 잠금 권한 등록" 버튼으로 기기 관리자 권한을 등록해야 한다.
- 위 두 가지가 모두 불가능하면 알림으로 수동 종료를 안내만 한다.

## 필요한 권한/설정
- 알림 권한 (Android 13+에서 팝업으로 요청됨)
- 배터리 최적화 예외 처리 권장 (설정에서 이 앱을 "제한 없음"으로 지정하지 않으면 타이머 도중 서비스가 종료될 수 있음)

## 포함된 기능
- 분 단위로 종료 시각 설정 → 시작
- 진행 중 알림에 남은 시간 표시, 알림에서 바로 취소 가능

## 시계 타이머 (2단계, 추가됨)
메인 화면의 "시계 타이머 열기" 버튼으로 진입.
- **초기화**: 표시 시간을 00:00:00으로 되돌림 (실행 중이면 카운트다운도 중단)
- **+5분 / +10분**: 설정 시간을 즉시 늘림. 카운트다운 실행 중에도 눌러서 시간을 늘릴 수 있음 (해당 시점부터 새 남은 시간으로 재시작)
- **시작/일시정지**: 카운트다운 제어
- **정지**: 초기화와 동일하게 00:00:00으로 되돌림

## 아직 미포함 (다음 단계)
- 갤러리 편의 기능
- 메세지 편의 기능

이 두 가지는 범위가 넓어서(예: 갤러리 - 정렬/삭제 자동화, 중복 사진 찾기, 앨범 자동 분류 중 어느 것인지 등) 구체적인 요구사항을 확인한 뒤 별도 모듈로 추가하는 것을 권장함.
