db = db.getSiblingDB('schedule-university');


db.createUser({
    user: "abuser",
    pwd: "abuser",
    roles: [
        { role: "readWrite", db: "schedule-university" },
        { role: "dbAdmin", db: "schedule-university" }
    ]
});

print("Пользователь abuser успешно создан");
