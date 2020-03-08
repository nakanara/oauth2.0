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


> OAuth 2.0 프로바이더를 설정할 때는 모든 클라이언트 세부 정보를 메모리상에 선언해서 저장하지 말고 데이터베이스를 이용해서 저장하는 방식 고려

## 암시적 그랜트 타입 지원


> 암묵적 그랜드 타입의 경우 OAuth 2.0 스펙에 의거해서 액세스 토큰을 발급할 수 없음, 브라우저 내에서 실행되는 애플리케이션을 이용할 때는 항상 사용자가 있어야 하기 때문에 사용자는 필요하다면 언제나 서드파티 애플리케이션에 권한을 위임하기 때문에 암시적 그랜트 타입의 동작 방식이 적합하다고 볼 수 있음.
또한 인가 서버는 사용자의 세션을 인식하기 위한 조건과 리소스 소유자에게 인증을 수행하게 요청하거나 클라이언트에 권한을 다시 요청하지 않도록 많은 조건을 갖고 있다. 암시적 그랜트 타입에서 리프레시 토큰을 발급하지 않는 또 다른 이유는 암시적 그랜트 타입이 리프레시 토큰과 같은 기밀 데이터를 보호할 수 없는 애플리케이션을 위한 것이기 때문


1. 인증 

```
http://localhost:8080/oauth/authorize?client_id=clientapp&redirect_url=http://localhost:9000/callback&response_type=token&scope=read_profile&state=xyz
```

결과

```
http://localhost:9000/callback#access_token=e379da32-d64b-4e64-a1d5-3759e655da18&token_type=bearer&state=xyz&expires_in=119
```

2. API 호출
```
curl -X GET http://localhost:8080/api/profile -H "authorization: Bearer 147dab74-f512-4af5-bb36-ecd19b4ffeed"

# 결과 - 시간 초과
{"error":"invalid_token","error_description":"Access token expired: e379da32-d64b-4e64-a1d5-3759e655da18"}

# 성공
{"name":"admin","email":"admin@mailnator.com"}
```


## OAuth 2.0으로의 전환을 위한 리소스 소유자 패스워드 자격증명 그랜트 타입

> 해당 그랜트 타입은 사용자의 자격증명 정보를 요구하기 때문에 가능하면 사용하지 말아야 한다(OAuth 2.0은 접근 권한을 위임함으로써 이를 해결). 하지만 사용자의 자격증명 정보를 공유하는 방식에서 OAuth 2.0으로 전환할 때 전략적으로 언급할 필요가 있다. 또한 클라이언트와 OAuth 2.0 프로바이더가 동일한 솔루션에 속할 때는 안전하게 사용될 수도 있다.

> 가능한 사용하지 말며, OAuth 2.0 프로바이더가 동일 솔루션일 경우만 사용 권고, 사용자는 자신의 자격증명이 공유되는 것을 신뢰할 수 있어야 함, 또한 애플리케이션은 액세스 토큰을 얻기 위해 전달한 사용자 이름과 패스워드를 저장하지 않고 버려야 하는 점


패스워드 방식일 경우 `AuthenticatioinManager` 필요, Config override필요

```java
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    // password 인가시 필요
    @Autowired
    private AuthenticationManager authenticationManager;       

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("clientapp")
                .secret("123456")
                .redirectUris("http://localhost:9000/callback")
                .authorizedGrantTypes("password") // Password 인가 그랜트 타입
                .scopes("read_profile", "read_contacts");
    }

    // password 인가시 
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
    }

}
```

1. 인증 

```sh
curl -X POST --user clientapp:123456 http://localhost:8080/oauth/token -H "accept: application/json" -H "content-type: application/x-www-form-urlencoded" -d "grant_type=password&username=admin&password=123&scope=read_profile"
```

결과

"토큰 만료시간을 설정하지 않을 경우 43200초로 설정"

```json
{   
    "access_token":"d9b1283b-d840-4868-89c2-eb9ac643bca6"
    ,"token_type":"bearer"
    ,"expires_in":43199
    ,"scope":"read_profile"
}
```

2. API 호출

```sh
curl -X GET http://localhost:8080/api/profile -H "authorization: Bearer d9b1283b-d840-4868-89c2-eb9ac643bca6"
```

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