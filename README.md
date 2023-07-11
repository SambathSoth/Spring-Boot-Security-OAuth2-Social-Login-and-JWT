# Spring Boot Security OAuth2 Social Login and JWT

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.1-brightgreen.svg) ![OAuth2](https://img.shields.io/badge/OAuth2-Social%20Login-blue.svg) ![JWT](https://img.shields.io/badge/JWT-Token-orange.svg)

A comprehensive guide and example implementation of Spring Boot Security with OAuth2 social login and JWT token authentication. This project provides a boilerplate codebase and step-by-step instructions for integrating OAuth2-based social login (e.g., Google, Facebook, GitHub) into a Spring Boot application, along with JWT token-based authentication.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Contributing](#contributing)

## Introduction

Securing a web application is crucial in today's digital landscape. This project demonstrates how to implement authentication and authorization using Spring Security, OAuth2, and JSON Web Tokens (JWT) in a Spring Boot application. It enables users to sign in to the application using their social media accounts, and provides a secure and stateless authentication mechanism using JWT tokens.

## Features

- Social login integration with popular OAuth2 providers (Google, Facebook, GitHub)
- JWT token generation and validation for stateless authentication
- User registration and management
- Authorization and role-based access control (RBAC)
- Password encryption using BCrypt hashing algorithm
- Well-documented and easy-to-follow instructions

## Requirements

- Java Development Kit (JDK) 17 or above
- Maven 3.6.x or above
- An internet connection to access the OAuth2 provider APIs

## Installation

1. Clone the repository:

```bash
git clone https://github.com/your-username/Spring-Boot-Security-OAuth2-Social-Login-and-JWT.git
```

2. Navigate to the project directory:

```bash
cd Spring-Boot-Security-OAuth2-Social-Login-and-JWT
```

3. Build the project using Maven:

```bash
mvn clean install
```

## Configuration

Before running the application, you need to configure the OAuth2 provider credentials in the `application.properties` file. Follow these steps:

1. Open the `src/main/resources/application.properties` file.
2. Configure the OAuth2 provider details by replacing the placeholders with your actual credentials:

```properties
spring:
    security:
        oauth2:
            client:
                registration:
                    google:
                        client-id: <your-client-id>
                        client-secret: <your-client-secret>
                        scope:
                            - email
                            - profile
                    facebook:
                        client-id: <your-client-id>
                        client-secret: <your-client-secret>
                        scope:
                            - email
                            - public_profile
                    github:
                        client-id: <your-client-id>
                        client-secret: <your-client-secret>
                        scope:
                            - user:email
                            - read:user
                provider:
                    facebook:
                    authorizationUri: https://www.facebook.com/v3.0/dialog/oauth
                    tokenUri: https://graph.facebook.com/v3.0/oauth/access_token
                    userInfoUri: https://graph.facebook.com/v3.0/me?fields=id,email,first_name,middle_name,last_name,name,verified,picture.width(250).height(250)
                    userNameAttribute: id
```
        
## Usage

1. Run the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvement, please submit a pull request or open an issue in this repository. Please make sure to follow the code of conduct.
