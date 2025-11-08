## Next Steps
- [x] add GitHub oauth2 client registration
- [x] verify redirect to GitHub works
- [x] verify handshake completes and authorized client is stored
- [x] update GitHub service to use the stored client
- [x] use the GitHub service to show the user's repos
- [ ] think about how to keep/restore my own authentication from `/authorize` to `/callback`
- [ ] refactor GitHub service to build webclient using ServerOAuth2AuthorizedClientExchangeFilterFunction
  - designed to work with web requests that have a security context