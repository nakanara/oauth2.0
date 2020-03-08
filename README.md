# oauth2.0

2장 OAuth 2.0 프로바이더 구현

출판된 시점의 버전이 변경되면서 기능이 예제처럼 작동하지 않아서 버전을 변경
spring.io 에서 그대로 진행하였을 때 문제 발생

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <!--<version>2.2.4.RELEASE</version>-->
    <version>1.5.4.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

```java
// OAuth2AuthorizationServer 

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) {
        clients.inMemory() // 저장 방식
                .withClient("clientapp") // 클라이언트 명
                .secret("123456")   // 스크릿
                .redirectUris("http://localhost:9000/callback") // 리다이렉션 url
                .authorizedGrantTypes("authorization_code") // Authorized Grant Type
                .scopes("read_profile", "read_contacts");

    }
}

```
### 테스트 

1  테스트 URL 

http://localhost:8080/api/profile

2 인증 시작

http://localhost:8080/oauth/authorize?client_id=clientapp&redirect_url=http://localhost:9000/callback&response_type=code&scope=read_profile

결과: http://localhost:9000/callback?code=Unm7gL


```shell script
# 3 인증 진행
curl -X POST --user clientapp:123456 http://localhost:8080/oauth/token -H "content-type: application/x-www-form-urlencoded" -d "code=Unm7gL&grant_type=authorization_code&redirect_url=http://localhost:9000/callback&response_type=code&scope=read_profile"
# 결과
# {"access_token":"92221944-56a4-4531-a3d5-2132c31c49de","token_type":"bearer","expires_in":43012,"scope":"read_profile"}

# 4 프로파일
curl -X GET http://localhost:8080/api/profile -H "authorization: Bearer 92221944-56a4-4531-a3d5-2132c31c49de"
# 결과
# {"name":"admin","email":"admin@mailnator.com"}
```


실제 OAuth 2. 프로바이더를 설정할 때는 모든 클라이언트 세부 정보를 메모리상에 선언해서 저장하지 말고 데이터베이스를 이용해서 저장하는 방식 고려

### 인증 종류
Authorization Code Grant

1. Authorization Code Grant

* 서버사이드 코드로 인증하는 방식
* AuthorizationServer는 로그인 기능 제공
* 권한 서버가 클라이언트와 리소스 서버간의 중재 역할
* Access Token을 바로 클라이언트로 전달하지 않아 잠재적 유출을 방지
* 로그인시에 페이지 URL에 response_type=code 설정

2. Implicit Grant

* token과 scope에 대한 스펙 등은 다르지만 OAuth 1.0a과 가장 비슷한 인증 방식
* Public Client인 브라우저 기반의 애플리케이션(Javascript application)이나 모바일 애플리케이션에서 이 방식 사용
* OAuth 2.0에서 가장 많이 사용하는 방식
* 권한코드 없이 바로 발급되서 보안에 취약
* 주로 Read Only 서비스에 사용
* 로그인시 페이지 URL에 response_type=token 설정

3. Resource Owner Password Credentials Grant

* Client에 계정 정보 저장 후 직접 access token을 받아오는 방식
* Client를 믿을 수 없을 때에는 사용하기에 위험하기 때문에 API 서비스의 공식 애플리케이션이나 믿을 수 있는 Client에 한해서만 사용을 추천
* 로그인시에 API에 POST로 grant_type=password로 전달

4. Client Credentials Grant

* 애플리케이션이 Confidential Client일 때 id와 secret을 가지고 인증하는 방식
* 로그인시 API에 POST로 grant_type=client_credentials 전달

### Token

- Access Token 

위 4가지 권한 요청 방식 모두 절차를 정상적으로 처리 된 경우 클라이언트에게 Access Token이 발급, 이 토큰은 보호된 리소스에 접근 할 때 권한 용도로 사용됩니다 
문자열로 반환되며 클라이언트에서 발급된 권한을 대변하게 됩니다. 계정 정보 등 인증에 필요한 형태들을 이 토큰 하나로 표현함으로써, 리소스 서버는 여러 가지 인증 방식에 각각 대응 하지 않아도 권한을 확인 할 수 있습니다.

- Refresh Token

한번 발급받은 Access Token은 사용할 수 있는 시간은 제한되어 있습니다. 사용하고 있던 Access Token이 유효기간 종료 등으로 만료되면, 새로운 Access Token을 취득해야 하는데 그 때 Refresh Token을 사용합니다.
권한 서버가 Access Token을 발급해주는 시점에서 Refresh Token도 함께 발급하여 클라이언트에게 전달되기 때문에, 전용 발급 절차 없이 Refresh Token을 미리 가지고 있을 수 있습니다. 
토큰의 형태는 Access Token과 동일하게 문자열 형태입니다.

- 토큰의 갱신 과정

클라이언트가 권한 증서를 가지고 권한 서버에 Access Token을 요청하면, 권한 서버는 Access Token과 Refresh Toeken을 함께 클라이언트에게 전달됩니다.
그 이후 클라이언트는 Access Token을 사용하여 리소스들을 요청하는 과정을 반복하며, 일정 시간이 흐른 후 액세스 토큰이 만료되면, 리소스 서버는 이후 요청들에 대해 정상 결과 대신 인증 만료 오류를 응답하게 되며, 
그 시점에 Refresh Token을 이용하여 새로운 Access Token을 새롭게 발급 받을 수 있습니다.


# 참고

- https://minwan1.github.io/2018/02/24/2018-02-24-OAuth/
- https://showerbugs.github.io/2017-11-16/OAuth-%EB%9E%80-%EB%AC%B4%EC%97%87%EC%9D%BC%EA%B9%8C
- https://velog.io/@rohkorea86/oAuth%EC%9D%B4%EB%A1%A0