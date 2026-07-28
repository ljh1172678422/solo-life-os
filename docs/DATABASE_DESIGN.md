# 数据库设计


## User


user

------

id

nickname

avatar

city

created_time



## User Preference


user_preference

------

user_id

interest

budget

lifestyle



## Daily Plan


daily_plan

------

id

user_id

date

status



## Activity


activity

------

id

title

type

location

start_time

end_time



## Mood Record


mood_record

------

id

user_id

mood

tags

created_time



## Goal


goal

------

id

user_id

name

type

progress



## Favorite


favorite

------

id

user_id

target_type

target_id



## Memory


ai_memory

------

id

user_id

memory_type

content

importance
