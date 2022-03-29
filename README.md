![](logo.svg)

[![Docker][docker-image]][docker-url]
[![Conventional Commit][commit-image]][commit-url]

## Event Emitter

Event emitter allows you to produce events using REST.

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
    "topicName": "tIZVfnGOaMRdnIOFVTOEZMxyVRXPKwBKeKiGPZovIfdOFeRIcQDfgjzIkTOIdhqcbFFbIinoiaflbTsIYicqsOeirXBjPCJCXPzmmYQeXwgzLWHHZoTwoIBCzUJivNzJOsHnQwzckDkXOhXVZTZqyEekfoZPcDQXygpuYbplthQFECFbkrArchkonSYvusYjDawNmCTJGrbNSuEcXSIkqCFUzaYebglLzGKqnYGXnnXAYZLvLuBMhMbzzlBBqZjHVcBKeKmEdjfAfrlEXMkrFsYTajLaMiRJscpKEkXeXJGtapbiclWPsbkQaSMbOBjlwcJQMEeqiHntWfyWqjQfQANIDlCOaTFUKulrRwwPdeiMDVyCvKQbTxxdotgdIuhongaCiPweEPWxobVJuTfVSUzBdIDRvRmMnhpImjwDrQhZbHIgObDyYqZwxcFqAeYHzvrgdoQzPtyUdZJkvFuvrHhRSITEipvVrTvqQgezwBcUpwgNVdsAEEVzVFpHgQxvhARyhvEVIwinRsdeMeNxFEMRQXtICksJMmtqNitGSFyAqaaDOkPDPTjCTFZWbnzmvPjJMPbalFQOPEkdpCCqHEOkfioIJOkHbaiZtEDUHwsubfJDLdqrLsqPUVhBlROmuFeoxdGcUgLjkEUpEVscfoziZDGylDbfczgYXxoKhKPpKammeEGEXldqhdNx",
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

## Contributing
Want to contribute to the code? Please take a moment to read our [Contributing](CONTRIBUTING.md) guide to learn about our development process.

[docker-url]: https://harbor.backbase.eu/harbor/projects/3/repositories/event-emitter
[docker-image]: https://img.shields.io/badge/docker-harbor.backbase.eu%2Fdevelopment%2Fevent--emitter%3A2022.02-blue

[commit-url]: https://conventionalcommits.org
[commit-image]: https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg