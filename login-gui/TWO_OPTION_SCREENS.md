# Two-option screens in login-gui

Question text resolved from `src/locale/en.ts`. "Auto-highlighted" = the button with `autoFocus` (initial keyboard focus).

| File | Question (EN) | Answer 1 (Button) | Answer 2 (Button) | Auto-highlighted |
|---|---|---|---|---|
| [`NudgeAppView.tsx`](src/components/NudgeApp/NudgeAppView.tsx) (rendered by [`routes/confirm/route.tsx`](src/routes/confirm/route.tsx)) | "Want to sign in quicker and more securely next time?" Get the eduID app and securely sign in without passwords or accessing your email. It will only take a minute. | "No thanks" (outline) | "Get it now" (primary) | Answer 2: "Get it now" |
| [`routes/remember/route.tsx`](src/routes/remember/route.tsx) | "Stay signed in?" You won't need to log in again next time. Only do this on a device you don't share with others. | "No, not needed" (outline `Button`) | "Yes, stay signed in" (`LoadingButton`) | Answer 2: "Yes, stay signed in" |
| [`GetAppView.tsx`](src/components/AppEnrollment/GetAppView.tsx) | "Did you already install the app? Please continue." (title: "Activate your eduID app") | "Back" (outline) | "Continue" (primary) | Answer 2: "Continue" |
| [`AppRequiredView.tsx`](src/components/AppRequired/AppRequiredView.tsx) (main screen) | "Almost there". To continue to {{serviceName}} you must use the eduID app. | "I refuse" (outline, opens confirm dialog) | "Download the eduID app" (primary) | Answer 2: "Download the eduID app" |
| [`AppRequiredView.tsx`](src/components/AppRequired/AppRequiredView.tsx) (confirm dialog) | "Get the eduID app". Login without the eduID app is not allowed. You will not be able to log in to {{serviceName}}. | "I still refuse" (outline `Button`) | "Download the eduID app" (`AlertDialogAction`) | Answer 2: "Download the eduID app" |
| [`LinkOutcome.tsx`](src/components/LinkOutcome/LinkOutcome.tsx) | Not a literal question; the info text offers a choice, e.g. "You can try to link to another institution or proceed to {{serviceName}}." Title/text vary per scope (`affiliationMissing`, `validNameMissing`, `eppnAlreadyLinked`, `subjectAlreadyLinked`, `iapAssuranceMissing`, `attributeMissing`). | "Proceed" (outline) | "Retry" (primary) | Answer 2: "Retry" |
| [`ValidateNamesView.tsx`](src/components/Stepup/ValidateNamesView.tsx) | "Prove who you are": "Do you have an account at an educational or research institution?" (description only shown when the institution option is offered) | "Use your institution account" (`Button`) | List of buttons, hint "No institution account? Choose one of the following methods:": "Use your bank" (iDIN) / "Use a European ID" | "Use your institution account" if shown; otherwise "Use your bank" (first list item) |
| [`RecoveryCodeView.tsx`](src/components/AppEnrollment/RecoveryCodeView.tsx) | Not a question. "Keep the code in a safe place". This code is currently active, but you can generate a new one at any time. | "Copy the code" (`Button`, label toggles to "Copied") | "I saved the code. Continue" (`LoadingButton`, outline) | Answer 2: "I saved the code. Continue" |

## Notes
- `AppRequiredView` dialog: `AlertDialogAction` carries the explicit `autoFocus`; "I still refuse" does not.
- `ValidateNamesView` is a method picker rather than a strict two-way choice; the number of options depends on `mode`.
- `RecoveryCodeView` has two actions but no choice; `autoFocus` is on the "Continue" button.
- Every screen in this table now has an auto-highlighted button (none left without).
