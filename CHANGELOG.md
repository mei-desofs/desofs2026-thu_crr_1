# Changelog

## [1.3.0](https://github.com/mei-desofs/desofs2026-thu_crr_1/compare/v1.2.0...v1.3.0) (2026-05-18)


### Features

* added logout to security config ([25e9d60](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/25e9d60b493809b868ad66ba96c6fa4856a46031))
* enhance JWT authentication with issuer and audience validation, update invite logic, and improve test coverage ([5f05d3e](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/5f05d3e24f2d24f7a67cb9f36a92aaedc8ddba2d))


### Bug Fixes

* add more envs in dast-scan ([58d123d](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/58d123dd31431750b30eec943e95bc6e50d6c2ba))
* add more envs in dast-scan ([b61382d](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b61382dceae84debd72de94750c01f1c351802b4))
* allow access to actuator health endpoint in security configuration ([1f35601](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1f35601113697ea85e3c062e2f474576fa1b78fd))
* fix /confirm email in register ([4c908d6](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/4c908d641b5e0db1169d6669b11c9149bed3535b))
* fix duplicated check for Register Email ([#8](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/8)) ([a11ed84](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/a11ed8436aae462c71e2fa883345c51b3312588b))
* fix logout security ([c12f688](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c12f6887151dd7c61fd09124f51e9e7798b9e75a))
* fix security config and some fixes in report ([bc1c7f8](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/bc1c7f8a0d01d69d1980e0cb1524769d2d49142e))
* fix security config and some fixes in report ([3252c16](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/3252c166faff566ef6a5bb33d4cef8e81cdefdf0))
* fix tests erros for new register ([#8](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/8)) ([fcae4c6](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fcae4c6864111b8d4ee255342a7c92be8e2e3914))
* restrict product endpoints to users with MANAGER role and update session management policy documentation ([e5e438c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/e5e438ce6c2468768dc80cbd0bc00e6f7b4cf12a))
* small fixes for Email in Register([#8](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/8)) ([898dbac](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/898dbaca9ea065f1b0a3ae609a571d5330f3110d))
* sonar workflow ([fcfdd4a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fcfdd4a39b3c05d322bcbf398266940532c22c2f))
* update invite confirmation endpoint and update authentication logic ([b9353ca](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b9353ca268646a93dae8002cd43171a378d2c288))
* warning in sonar ([4091a53](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/4091a5356824ae7eb3ae59fa9acc51f905043db9))
* warning in sonar ([83453f8](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/83453f89cbc3b9ad93499e8eb04845b45fa45baf))

## [1.2.0](https://github.com/mei-desofs/desofs2026-thu_crr_1/compare/v1.1.0...v1.2.0) (2026-05-16)


### Features

* add additional constructor to ErrorResponse for enhanced error handling ([ecc3bda](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ecc3bdaa1cae7124267bbc50949c56d731dba32d))
* add backups folder to gitignore ([c90d6e3](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c90d6e333c2e0c5e0294a20034efa1cdb46e39e0))
* add cookies to logout ([2795440](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/2795440aef404169d812ea8d5ff8e31d1fbe9380))
* add dependency scanning workflow to enhance security analysis on feature.yml ([332cd4c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/332cd4c08874e236a3f7fcf7b1c7d9553a51aefb))
* add junits to the products backup feature ([fcd6eee](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fcd6eeef9430f5098d08543c012f580dfdc1a27b))
* add log of userId in Refresh ([#11](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/11)) ([bd597dc](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/bd597dcff6a63e0d3e42170da039adc7fc1d2ae3))
* add logs to gitignore ([4658b6b](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/4658b6b2c6b6f8a75004485a7ea1b936953a61f6))
* add MFA to login ([#9](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/9)) ([f1e5d66](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/f1e5d665922cbf482af0fb56d2b27432f1383237))
* add rate limit to endpoints ([#9](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/9) & [#11](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/11)) ([c3b39f4](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c3b39f438a20a62a6653e32ead905383448b41a0))
* add ratelimiter to register and logout ([2795440](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/2795440aef404169d812ea8d5ff8e31d1fbe9380))
* add sast to docker needs ([772923c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/772923ce0ba9925d1e152e906c3177b1146973ba))
* add sast to docker needs ([3e877a0](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/3e877a0d758f24a4330c8dd8dc90760b41817d40))
* add unit tests for AuthAuditLogger, BusinessException, Cart, and Order classes ([de7b817](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/de7b81719d2ffc297daa6c490e164ea5c978b895))
* enable rate limiting and authorization for user invitation endpoint ([6ee392b](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/6ee392b2f0bd431c01d2097cf1f5230670b411da))
* enhance user invitation flow with confirmation endpoint and improved error handling ([1bb2d33](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1bb2d33883d2c26031b40068c6490731d5523088))
* enhance user invitation process with audit logging and error handling ([#4](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/4)) ([49f9041](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/49f9041cb75c20383f85d90a5de1adc89844ffea))
* implement password reset and update functionality with validation and logging ([12c0fb5](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/12c0fb513be33ff3a9917f56ce40164fc8ab4099))
* implemented register and logout ([711794c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/711794cc3d7a89856c056db7c2b5aa432340c894))
* improve error messages in ExceptionToSafeResponseMapper and adjust minimum coverage threshold in pom.xml ([c713567](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c713567aa2dfe0f55ce4eb539ddfe8511994fedf))
* remove role restriction for user invitation endpoint ([5efbf97](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/5efbf97846c0df0ac632e09d64d4d779c1e7c453))
* security-container ([#119](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/119)) ([7601272](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/76012725c797aa0798345d785f151b81bd702336))
* security-container ([#119](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/119)) ([fc82edf](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fc82edf4f734e9109fb6735beb7313fc538f5f69))
* update password update flow to use access token and enforce minimum password length ([0fc51be](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/0fc51be4fb9c1653cd552ad03973125f4f4d84a8))
* update Supabase configuration and fix redirect options in authentication client ([9ab9f95](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9ab9f950b92f916155e812f79f3930722da66909))
* update Tomcat version to 11.0.22 and add dependency for CVE-2024-50379 ([0ae59c4](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/0ae59c45720941b1e2aab2131613784e22ea4873))
* use cookies in login and refresh token ([#9](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/9) & [#11](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/11)) ([da4329c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/da4329c05b9c723c864bbe814f302e3c3df6657c))


### Bug Fixes

* fix:  ([d6dfe82](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d6dfe825645ab30b4d49226dd8e5570fe607f6c9))
* add 1)workflow_dispatch  to container-security.yml ([bb61679](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/bb61679fd0096ccb751de38c7d22aec159a32a3c))
* add email check ([ff93765](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ff9376510114c793f6353daec0f962130c9781d5))
* add ratelimit to refresh endpoint ([1b8fe9c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1b8fe9c8aba09af4d444bde9c6dabcc926219691))
* add secret jwk to test properties ([7a2e742](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/7a2e7426bdae941f0372033b76c9c0a62e5cb0d6))
* add wrongfully removed authController logger ([3931318](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/3931318f028505b88f480a2f232f528f8df7918c))
* container-security theshold from high to critical ([3267276](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/326727677af024c10dec7c74959211fdc5b8a6e4))
* critical vunab from Dockerfile ([478c6aa](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/478c6aa6345ccf0640c85de1ae72fdde507f16a2))
* increase minimum coverage ratio to 70% in pom.xml ([6585c84](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/6585c84c4ae792944e4f0efcb80a63622ea9b4c2))
* increase minimum coverage ratio to 80% in pom.xml ([2db5569](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/2db55694930c13c7b14eb4459867b3f049c74d71))
* increase minimum coverage ratio to 90% in pom.xml ([56b8ff5](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/56b8ff5b6d8eb146742234be523a57ab7a6b469f))
* logout using cookies now ([4ebcc48](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/4ebcc48ee11b4df331d3549e15de6271016e709c))
* new job for container-security ([b3cd753](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b3cd753d27aeb7658c3b1e11bd6b7782f547d57f))
* Potential fix for pull request finding ([9a1825f](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9a1825f3f2576e88a17decaf9b2f9f0a525c6b30))
* remove comments ([c027f5f](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c027f5fbd88c09c2a96a10d106c5524ba5229eae))
* remove email local email check ([2795440](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/2795440aef404169d812ea8d5ff8e31d1fbe9380))
* remove unecessary comments ([afdf821](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/afdf8218714c9a3cdb3a4f653a7cbfa5826551f7))
* restrict backup endpoint command ([61e7a0b](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/61e7a0b80ecc7e969416007b5a1d29983b2503fc))
* string parsing ([e2b41d3](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/e2b41d3975935305a918f31f14b636b591b1729a))
* switch tool from trivy to Snyk  ([#119](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/119)) ([af1fe6d](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/af1fe6d23072f7b0ba13d36eece360e9bb5175cc))
* Use only sonar token secret instead of inherit all ([af34d41](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/af34d41464ef7c398f350c1a195d424dfa064a3b))
* user id ([fcb1f9b](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fcb1f9bc025462a5f7cbd99202a360b78c0dec3e))
* User text small fix ([decd67a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/decd67ac6422aaccd4a07cee034141bd74af5561))

## [1.1.0](https://github.com/mei-desofs/desofs2026-thu_crr_1/compare/v1.0.0...v1.1.0) (2026-05-11)


### Features

* add build-test pipeline step, and create basic pipeline to main, feature and dev ([9c8bdf9](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9c8bdf9068895d8f356735e53f0800ba49286867))
* add Docker deployment workflow and CORS configuration ([f13860c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/f13860c0a4ffef2cda72f71954d394be8041cf5b))
* add product audit logging and validation to product creation ([91876b2](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/91876b21fea1b5055199fe64aef55bf98404bc66))
* add ProductAuditLogger mock to ProductServiceImplTest ([e1a0282](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/e1a0282d4aa1c59e63ee2e4b49b6aa4c6e05389c))
* implement rate limiting for invite requests ([#111](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/111)) ([fee3020](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/fee302070a5f5ee8ae703bdbf3d6d39f8533fc6d))
* integrate dependency scanning workflow into main and dev configurations ([50a1476](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/50a1476c526a81c5c39f39000e3dc34e143d2055))
* integrate dependency scanning workflow into main and dev configurations ([ccad4cf](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ccad4cf29b1a5dd8d5a27db77e4c68608cff11ac))
* refactor token generation in RateLimitIntegrationTest for improved readability ([434117a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/434117aec8d9c27c60944185e5d6def419022d30))
* update logging categories for product audit and adjust rate limit type ([23b828d](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/23b828d635666b8a2ea822b221c373c27422fea7))


### Bug Fixes

* add debug param ([27d1216](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/27d1216baea257331b854bc117d031b3abf1f56f))
* add org slug ([077adc3](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/077adc370fe0e8cb1da540b834697505f2d19fbc))
* fail when found one or more critical vulnerabilitis ([629f319](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/629f31902b261c817d7e5b73180f592c265b72d7))
* remove debug ([51f7253](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/51f72535c93a850f25115baee482836cf65b1ef1))
* remove unnecessary dependencies from deployment workflows ([c08bd22](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c08bd229c351b113f76d39c47788ca09c2c96b06))
* snyk step ([412e596](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/412e596ef5dddd1684ca0c41d4f79e5ae235ab1a))
* snyk step ([8df3f0a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/8df3f0a36c65db904278f2c003936145c99b1621))
* snyk step ([9debfac](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9debfac9b72b62eb8e6f0018d4f7955b756aa526))
* snyk step ([e0833e5](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/e0833e518da762778edc0c45df61c7df2e1c62e9))
* Snyk Vulnerability Scanning ([28512c6](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/28512c66ad952395022ec4b1cd62c5b05dbdb667))
* test on feature branch ([d3a5e79](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d3a5e79c6d99e6f3edfcbdab9299c106550400ec))
* update permissions for deployment workflow ([39fbe55](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/39fbe55ed77aac9a8dff09cd0670fddbe989ccfc))
* update Spring Boot version to 4.0.6 in pom.xml ([c6d4490](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c6d44901b9a3255966e26c23ed5963de9ebc5c56))

## 1.0.0 (2026-04-30)


### Features

* feat:  ([1be7a18](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1be7a18edd10e323f9e91720380f518b7112ea97))
* add build-test pipeline step, and create basic pipeline to main, dev and feature branches ([#99](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/99)) ([06000d8](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/06000d8b3cb56b6efbf30b9f9508c6ed29470904))
* add cart, order ([#87](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/87)) ([b68c2af](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b68c2af40403033fc009075daea94de882854809))
* add missing products creation on the bootstrap ([462b389](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/462b389ee8725206d9d70c840c74ed29566fbf6b))
* add tests and value objects implementation ([4ba734c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/4ba734c6f8a2b2445c327be64cab7e39501eaa62))
* added my part in global ([f382921](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/f3829215c5f6c64ee9c9a7261b3420d227f10184))
* added my part in global ([5fe6645](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/5fe6645d9bdb444dfe68d58d93ec45aa71192c36))
* base implementation invite users [#4](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/4) ([452e597](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/452e597c36eae02e7eba1f23d304e6ab2d6b841b))
* base implementation invite users [#4](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/4) ([1a1cbaf](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1a1cbafba543393dd751b00c590e2ed06800228d))
* change dto classes to records ([#6](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/6)) ([bdfb631](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/bdfb631f8dbd5a9f6bec4b113f536bffe189af8b))
* create products WIP ([#6](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/6)) ([49322ff](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/49322ffdf159f8363f3b0faaa237eb2fbe14776e))
* create value objects of customer aggregate ([9a157fe](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9a157fe44e91bb54b41f9e8b2fcfdcb7b003097c))
* endpoint to get the products by a name ([#7](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/7)) ([da576ee](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/da576eeeec50f67421a2dc4c50817fdf6cb3ccb6))
* Implement customer domain and update user domain ([#58](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/58)) ([c97da4f](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/c97da4fc62ef8fc21dd67602a1f007acee167586))
* initial release ([cb8924b](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/cb8924bf7182ab9ba386551a59c52b29797a7d7d))
* initial release to Test Realise Please ([ed6895a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ed6895a3e921223f252ced30104f5b097f7e84b4))
* syslog loggin server (local) ([#93](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/93)) ([d64f800](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d64f8002c093930a6732e35ffeacea40f9a54184))
* trigger initial release ([#103](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/103)) ([21ee8fb](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/21ee8fbc8756edcb578587ddc35daae2264f8d8e))


### Bug Fixes

* fix:  ([ca317e7](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ca317e7f2939daf75812c9ad4baad88c73b5e198))
* abuse cases ([#59](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/59)) ([d69182f](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d69182fdec450e1231d97ebd3fbe9be68b97b5e4))
* add created_at and updated_at ([#6](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/6)) ([d0ff3a6](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d0ff3a6d73d1b90e5449a9c936b583360d75d44f))
* add permissions for release-please workflow call ([#104](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/104)) ([268ee0e](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/268ee0ec8104819e08617f699827756094d1b056))
* added workflow_dispatch in release.yml to test in main.yml realise please [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([9dd9a75](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/9dd9a75121663e2d69adc11d03fad5c3f35c63fc))
* call release.yml in main.yml [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([d2c0f40](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/d2c0f4052a99235861b00215bfbaeef6a1be6f4a))
* category aggregate to use DDD ([382a2a2](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/382a2a2790b8f715a7498274d1831ceba66bff73))
* dfds ([60b9f60](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/60b9f6026a8ae4953ec0a6630b148f3105c0ef55))
* dfds, abuse cases [#59](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/59) ([a583621](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/a5836215bf5c0f9786ef82cc414800c2ea698108))
* improve based on copilot feedback ([#6](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/6)) ([835c260](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/835c260d7cd7f0d3eb4bef11f8ec82d6f89b5b2f))
* improve based on copilot feedback ([#6](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/6)) ([bc388ae](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/bc388ae6a7cf939a4a016097e63028488fc20a70))
* invalid or in syslog config ([48887c6](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/48887c6a9b5cd24c3127b3041659f69de2f9f8b8))
* last minute fix ([73c7000](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/73c70009da67a3c0d24091d91507b34b7184eb21))
* merge conflict ([a80ff3c](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/a80ff3cdba94e3533aa3ef86488de3e428e8ca4b))
* merge conflicts ([ad13e7f](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ad13e7fa26c29c26352f41d2fa7ffac43b337505))
* minor fixs and add things to the .env.sample ([e76853e](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/e76853eea13afe9689f640782da8c9d3fb0be8b9))
* name release.please json [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([1247ed0](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/1247ed0f54b25ab2a17102488698ac562948260a))
* product aggregate to use DDD ([50a368e](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/50a368eff823ec60df64622e47f16e89e3395f3c))
* relase please manifest json version [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([8f27969](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/8f2796972a9718324fbbb88143fdbc5b405e0b52))
* release please json path [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([02415d5](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/02415d5a72ae60aaadf2b867ad1478f21a20b99f))
* release please jsons path fixx [#95](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/95) ([075a79a](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/075a79aec437efbc8f8f647eb9672d82ccab6788))
* release-please workflow call ([f42d6db](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/f42d6db01d1194de45405363a90c322d2f536b5e))
* release-please workflow call ([#105](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/105)) ([89c4788](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/89c4788f236a6439712ec5d815ce0eab5219eca5))
* remove sensitive variables, update .gitignore and adjust local setup ([10886a4](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/10886a40bb4b2fbb51cc4f39b2c929124f52fb29))
* remove unnecessary variable criation ([b3c9e7e](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b3c9e7e552cb05847992ee28d4af2762a0c6673f))
* remove unused files ([b4dd7f9](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b4dd7f990130406939c3a8fd945ec9b5a4eba059))
* remove use case diagram from branch ([b218490](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/b218490b5f65435af81f327eb39cda7a9dba5edd))
* syslog image version to prevent using diferent version ([216a315](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/216a315de054665272f7c6eb3269cb515ca1103e))
* Test/realise please ([#106](https://github.com/mei-desofs/desofs2026-thu_crr_1/issues/106)) ([ce8b046](https://github.com/mei-desofs/desofs2026-thu_crr_1/commit/ce8b04636170bf3df27ed7b3114f4a014ec22094))
