<p align="center">
  <img width="120" src="Backbase.png?raw=true" alt="Backbase Logo">
</p>

<p align="center">
Event emitter allows you to produce events using REST to the underlying message broker. This service is used for testing purposes only.
<p>

<p align="center"> 
    <a href="https://github.com/backbase/event-emitter/actions/workflows/main.yml">
        <img src="https://github.com/backbase/event-emitter/actions/workflows/main.yml/badge.svg" alt="Build" />
    </a>
    <a href="https://sonarcloud.io/summary/new_code?id=Backbase_event-emitter">
        <img src="https://sonarcloud.io/api/project_badges/measure?project=Backbase_event-emitter&metric=alert_status" alt="Quality Gate" />
    </a>
    <a href="https://sonarcloud.io/summary/new_code?id=Backbase_event-emitter">
        <img src="https://sonarcloud.io/api/project_badges/measure?project=Backbase_event-emitter&metric=sqale_rating" alt="Maintainability Rating" />
    </a>
    <a href="https://conventionalcommits.org">
        <img src="https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg" alt="Conventional Commits" />
    </a>
</p>

---

## Event Emitter

Event emitter allows you to produce events using REST.


### Custom Headers Configuration

In order to pass custom headers from HTTP request to Event - you need to configure key-value mapping pairs for such headers:

```yaml
backbase:
  event-emitter:
    custom-header-pairs:
      - http: X-LOB
        event: bbLineOfBusiness
```

The service will filter out the HTTP headers by the `http` field as key and will set the respective values to the Event under the matching `event` key from configuration.

For the listed example, if service receive the HTTP request with header `X-LOB: RETAIL` - this header would be converted added to the event as `bbLineOfBusiness: RETAIL`

If the conversion is not required - please set the same values to `http` and `event`

### Raw Event

```shell
curl --location --request POST 'http://localhost:8079/events/raw' \
--header 'Content-Type: application/json' \
--header 'X-LOB: RETAIL' \
--data-raw '{
    "destination": "Backbase.engagement.ProvisionItem",
    "eventType": "com.backbase.engagement.provisioning.messaging.dto.ProvisionItemCommand",
    "body": {
        "provisionedItem": {
            "itemUuid": "fdc0db99-8201-402e-b8f4-05fe154d44ba",
            "itemType": ""
        },
        "packageUuid": "86d994da-8907-494e-b579-4bc59cfa08e4",
        "destination": "",
        "override": true
    }
}'
```

### Event Spec

```shell
curl --location --request POST 'http://localhost:8079/events/com.backbase.dbs.messages.pandp.event.spec.v4.MessageDeliveredEvent' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "12345678",
    "recipient": "43934ff1-63e4-11ec-bd05-0242ac120004",
    "deliveringDateTime": "2021-07-28",
    "conversationId": "conversation_id",
    "sender": null,
    "subject": "This is subject",
    "body": "This is body",
    "category": "high",
    "expirationDateTime": "2021-08-28T14:16:27Z",
    "important": true,
    "recipientAddress": "email@gmail.com",
    "senderId": "sender_id",
    "topicName": "tIZVfnGOaMRdnIOFVTOEZMxyVRXPKwBKeKi...",
    "additionalProperties": {
        "alternativeTopicName": "altname"
    }
}'
```

```shell
curl --location --request POST 'http://localhost:8079/events/com.backbase.transaction.persistence.event.spec.v1.TransactionsAddedEvent' \
--header 'Content-Type: application/json' \
--data-raw '{
    "transactions": [
        {
            "arrangementId": "f48beab9-8229-11ec-8b7b-0242ac190005",
            "externalId": "U0000011",
            "description": "Quickbooks monthly subscription fee",
            "bookingDate": "2022-02-04T22:53:42.560053",
            "transactionAmountCurrency": {
                "amount": 300,
                "currencyCode": "EUR"
            },
            "id": "ac207959-3915-4673-9784-429e4a118e71",
            "counterPartyName": "Quickbooks",
            "creditDebitIndicator": "DBIT"
        },
        {
            "arrangementId": "f48beab9-8229-11ec-8b7b-0242ac190005",
            "externalId": "U0000011",
            "description": "Quickbooks monthly subscription fee",
            "bookingDate": "2022-02-04T22:53:43.000000",
            "transactionAmountCurrency": {
                "amount": 400,
                "currencyCode": "EUR"
            },
            "id": "ac207959-3915-4673-9784-429e4a118e71",
            "counterPartyName": "Quickbooks",
            "creditDebitIndicator": "DBIT"
        }
    ]
}'
```

```shell
curl --location --request POST 'http://localhost:8079/events/com.backbase.audit.persistence.event.spec.v1.AuditExportCompletedEvent' \
--header 'Content-Type: application/json' \
--data-raw '{
    "status": "successful",
    "userId": "19aa3423-4bc3-4624-bbbf-75064a441b44", 
    "link": "https://www.google.com/",
    "serviceAgreementId": "35494cc7-3266-11ec-ae7c-ce5ec8981a97"
}'
```

```shell
curl --location --request POST 'http://localhost:8079/events/com.backbase.account.statement.event.spec.v1.AccountStatementReadyEvent' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "19aa3423-4bc3-4624-bbbf-75064a441b44",
    "arrangementId": "f48beab9-8229-11ec-8b7b-0242ac190005",
    "date": "2022-10-17T12:00:00Z",
    "serviceAgreementId": "35494cc7-3266-11ec-ae7c-ce5ec8981a97"
}'
```

## Event Consumer

Event Consumer allows you to subscribe and consume events, store them in in-memory storage and retrieve via REST API.

Example of configuration to enable listening to ActiveMQ topics:
```
backbase.event-emitter.topic-names: Backbase.communication.push-low-priority,Backbase.communication.push-medium-priority,Backbase.communication.push-medium-priority,Backbase.communication.notifications-low-priority,Backbase.communication.notifications-medium-priority,Backbase.communication.notifications-high-priority,Backbase.communication.message-center-low-priority,Backbase.communication.message-center-medium-priority,Backbase.communication.message-center-high-priority,Backbase.communication.sms-low-priority,Backbase.communication.sms-medium-priority,Backbase.communication.sms-high-priority,Backbase.communication.email-low-priority,Backbase.communication.email-medium-priority,Backbase.communication.email-high-priority
```

### APIs
- Get all events
```
GET /events
```
- Get event by correlation id
```
GET /events/<correlationId>
```
- Delete all events (from internal memory storage)
```
DELETE /events
```

## How to produce docker image on your local
```shell
mvn clean package -Pdocker-image,local-client -Ddocker.repo.project=development -Djib.to.tags=local-11-05-2023-v1 -Djib.from.platforms=linux/amd64
```

## Contributing

First off, thanks for taking the time to contribute! Contributions are what makes the open-source community such an amazing place to learn, inspire, and create. Any contributions you make will benefit everybody else and are **greatly appreciated**.

Please adhere to this project's [code of conduct](CODE_OF_CONDUCT.md). For detailed instructions on repo organization, linting, testing, and other
steps see our [contributing guidelines](CONTRIBUTING.md)

#### Contributors

[![](https://contrib.rocks/image?repo=backbase/event-emitter)](https://github.com/backbase/event-emitter/graphs/contributors)

## License

This project is licensed under the **Backbase** license.

See [LICENSE.md](LICENSE.md) for more information.