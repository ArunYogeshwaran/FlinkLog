## IARC Content Rating Compliance

This app (FlinkLog) has a live IARC content rating on Google Play.
Global Rating ID: 735ef199-ebbb-8eae-8e00-3f5115cf251b
Company: Humanware Labs

### When to flag IARC re-rating

Any change that alters the app's content profile REQUIRES the developer to redo the IARC questionnaire before publishing. Flag a warning in your PR description or commit message if a change introduces any of the following:

- **Violence or graphic content** — even cartoon or implied violence
- **Sexual or suggestive content** — imagery, themes, or references
- **Profanity or crude language** — in UI text, error messages, placeholder content, or assets
- **User-generated content (UGC)** — chat, comments, forums, profile content, image uploads, or any feature where users can share content with others
- **Social/communication features** — friend lists, messaging, voice chat, multiplayer interactions
- **In-app purchases or real-money transactions** — including subscriptions, tips, donations, premium features
- **Ads or ad SDKs** — adding any advertising framework (AdMob, Unity Ads, etc.)
- **Location sharing or personal data collection** — especially if shared with other users
- **Gambling or simulated gambling** — loot boxes, gacha mechanics, spinning wheels for rewards
- **Controlled substances** — references to drugs, alcohol, or tobacco
- **Horror or fear-inducing content** — jump scares, disturbing imagery or themes
- **External links or redirects** — to unrated or uncontrolled web content

### What to do when flagging

Add this to your PR description:
```
⚠️ IARC RE-RATING MAY BE REQUIRED
This change introduces [describe what changed].
Before releasing this update, re-complete the IARC questionnaire in Google Play Console:
Play Console → Policy and programs → App content → Content ratings
Global Rating ID: 735ef199-ebbb-8eae-8e00-3f5115cf251b
```

### Why this matters

Publishing an update that changes the content profile without updating the IARC rating is a Google Play policy violation. It can result in the app being removed from the store. When in doubt, flag it — a false alarm is better than a policy strike.
