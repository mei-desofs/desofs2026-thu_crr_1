# Password Policy

## Password Requirements

The application only accepts passwords that comply with all of the following rules:

| Requirement               | Policy                                                                    |
| ------------------------- | ------------------------------------------------------------------------- |
| Minimum length            | At least `12` characters                                                  |
| Common passwords          | Common or widely used passwords are rejected                              |
| Context-specific words    | Application-related and easily guessable words are rejected               |
| User-specific information | Passwords must not contain identifiable parts of the user's email address |
| Breached passwords        | Passwords found in known public data breaches are rejected                |

## Common Password Protection

The application uses a local common password blocklist.

Passwords that appear in the common password list are rejected, even if they meet the minimum length requirement.

This prevents users from choosing passwords that are widely known, frequently used, or easily guessed.

## Context-Specific Password Protection

Passwords must not contain easily guessable words related to the application or common account terms.

The blocked context words include:

- `techstore`
- `tech store`
- `password`
- `welcome`
- `admin`
- `user`

Passwords containing any of these terms are rejected.

## User-Specific Password Protection

Passwords must not contain identifiable parts of the user's email address.

The application checks the local part of the user's email address, when available, and rejects passwords that contain it.

For example, a user with the email `john@example.com` must not use a password containing `john`.

This reduces the risk of users choosing passwords based on personal or account-related information.

## Breached Password Protection

The application checks whether a submitted password has appeared in known public data breaches.

This check is performed using the Have I Been Pwned password breach service.

Passwords found in known breaches are rejected, even if they satisfy the other password requirements.