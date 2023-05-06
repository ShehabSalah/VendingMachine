FlapKap Vending Machine
=======================
FlapKap Vending Machine is a RESTful web application that simulates a vending machine. The application is built using Spring Boot and utilizes Spring Security for authentication and authorization, Spring Data JPA for persistence, and H2 database for storage.

## Requirements
To run the FlapKap Vending Machine application, you need the following:

* Java Development Kit (JDK) 17 or later
* Apache Maven 3.6.3 or later
* The application is built using H2 database for storage (In-memory database). No additional database setup is required.

## Running the Application
To run the application, execute the following command in the root directory of the project:

```shell
mvn spring-boot:run
```
If you have multiple versions of Java installed on your machine, you can specify the version to use by setting the JAVA_HOME environment variable:

* On Windows:
```shell
set JAVA_HOME=C:\Program Files\Java\jdk-17
```
* On Linux:
```shell
export JAVA_HOME=/usr/lib/jvm/jdk-17
```

The application run on the default port **8080**. You can access the application via the following URL:

```shell
http://localhost:8080
```


## Testing the Application
To test the application, execute the following command in the root directory of the project:

```shell
mvn test
```

## Using the Application
The application is a RESTful web service that simulates a vending machine. The application has the following REST endpoints:

### Authentication Endpoints
* **POST /api/auth/login** - Authenticates a user and returns a JWT token
### User Endpoints
* **GET /api/v1/users** - Returns a list of all users (Requires ADMIN role)
* **POST /api/v1/users** - Creates a new user (no authentication required)
* **PUT /api/v1/users/{id}** - Updates an existing user (Requires ADMIN role)
* **DELETE /api/v1/users/{id}** - Deletes an existing user (Requires ADMIN role)
* **GET /api/v1/users/{id}** - Returns a user by id (Requires ADMIN role)
* **GET /api/v1/users/profile** - Returns the profile of the authenticated user (Requires authentication and any role can access it)
* **PUT /api/v1/users/profile** - Updates the profile of the authenticated user (Requires authentication and any role can access it)
* **PUT /api/v1/users/deposit/{amount}** - Deposits money into the authenticated user's account (Requires BUYER role)
* **PUT /api/v1/users/reset** - Resets the authenticated user's account balance to zero (Requires BUYER role)
### Product Endpoints
* **GET /api/v1/products** - Returns a list of all products with pagination (no authentication required)
* **GET /api/v1/products/{id}** - Returns a product by id (no authentication required)
* **GET /api/v1/products/my-products** - Returns a list of all products created by the authenticated user (Requires SELLER role)
* **POST /api/v1/products** - Creates a new product (Requires SELLER role)
* **PUT /api/v1/products/{id}** - Updates an existing product (Requires SELLER role)
* **DELETE /api/v1/products/{id}** - Deletes an existing product (Requires SELLER role)
* **POST /api/v1/products/buy/{id}** - Buys a product (Requires BUYER role)
### Default Credentials
The application has the following default credentials:

| Username | Password | Role       |
| -------- |----------|------------|
| admin    | admin123 | ROLE_ADMIN |
## API Documentation
The API documentation is available in Postman format and can be downloaded via the following URL:

```shell
https://apps.shehabsalah.info/flapkap/vendingmachine/postman_v1.zip
```

## License
The FlapKap Vending Machine application is licensed under the MIT `License`. See LICENSE for more information.