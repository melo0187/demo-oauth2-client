## Key Concepts: OAuth2 Client with User Authentication

This POC demonstrates how to authenticate users with your own system while allowing your application to interact with third-party APIs (GitHub) on their behalf.

### Core Architecture

**Problem Solved**: How to maintain your own user authentication while enabling OAuth2 flows with third-party providers in a stateless, horizontally scalable way.

### Key Components

#### 1. Dual Authentication System
- **Your Authentication**: HTTP Basic Auth (`user`/`password`) for your application
- **Third-Party OAuth2**: GitHub OAuth2 for API access on user's behalf
- **Isolation**: Each authenticated user gets their own OAuth2 clients

#### 2. Stateless Authentication Bridge
- **JWT State Parameter**: Encodes user's authentication in OAuth2 `state` parameter
- **RFC 7515 Compliant**: Uses Auth0 java-jwt library for proper JWT format
- **Signed & Expiring**: HMAC256 signature with 5-minute expiration
- **PreAuthenticatedAuthenticationToken**: Reconstitutes authentication from JWT

#### 3. OAuth2 Flow with User Context
```
1. User authenticates with your app (Basic Auth)
2. User visits /oauth2/authorize/github (authenticated endpoint)
3. Your app encodes user's Authentication in JWT state
4. GitHub redirects to /oauth2/callback/github (public endpoint)
5. Callback decodes JWT state to restore user context
6. OAuth2 client stored for specific authenticated user
```

#### 4. Automatic Token Management
- **ServerOAuth2AuthorizedClientExchangeFilterFunction**: Handles token injection
- **Reactive Security Context**: Automatically resolves current user
- **Token Refresh**: Spring Security handles OAuth2 token lifecycle
- **User-Specific Clients**: Each user's tokens isolated by principal name

### Benefits Achieved

- **Stateless**: No server-side sessions, JWT carries user context
- **Horizontally Scalable**: Works across multiple server instances
- **Security**: Signed JWTs prevent tampering, short expiration limits exposure
- **Clean Separation**: OAuth2 logic handled by Spring filters, not business code
- **User Isolation**: Each user's third-party tokens are completely separate

### Implementation Highlights

- **AuthenticationJwtService**: Encodes/decodes user authentication in JWTs
- **OAuth2ConnectionService**: Bridges JWT state to OAuth2 client storage
- **GitHubApiService**: Uses Spring's OAuth2 filter for automatic token handling
- **Security Configuration**: Protects all endpoints except OAuth2 callback

## Steps
- [x] add GitHub oauth2 client registration
- [x] verify redirect to GitHub works
- [x] verify handshake completes and authorized client is stored
- [x] update GitHub service to use the stored client
- [x] use the GitHub service to show the user's repos
- [x] think about how to keep/restore my own authentication from `/authorize` to `/callback`
- [x] refactor GitHub service to build webclient using ServerOAuth2AuthorizedClientExchangeFilterFunction