# Firebase setup

The app lets officers create their own email/password accounts. After
registration, it adds an active profile at `users/{UID}` in Firestore.

## 1. Enable sign-in

In Firebase Console, enable **Authentication -> Sign-in method ->
Email/Password**. Do not enable anonymous sign-in.

## 2. Self sign-up and password reset

1. On the app login screen, choose **Create account**.
2. Enter the officer's name, official email, and a password of at least six
   characters.
3. The officer can use **Forgot password?** on the login screen whenever a
   reset is needed.

The app creates the following profile for the authenticated officer:

```json
{
  "email": "officer@up.gov.in",
  "displayName": "Officer Name",
  "role": "OFFICER",
  "approved": true,
  "status": "ACTIVE"
}
```

## 3. Deploy the Firestore rules

Copy the contents of `firestore.rules` to **Firestore Database -> Rules**,
then publish. The rules let a signed-in officer create only their own fixed
profile and access only their own notices.

## 4. Revoke access

Disable or delete the Authentication user. Their locally encrypted data
remains on their original device, but it cannot be synced or restored without
an active account.

## 5. Production controls

Use a restricted Firebase administrator account, enforce password policy, and
periodically review Firestore access logs. Do not use Firestore test mode in
production.
