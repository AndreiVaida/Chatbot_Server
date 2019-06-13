select * from entry where description = "obișnui";
select * from entrydefinition where entryId = 38381;
select * from definition d where d.id in (select ed.definitionId from entrydefinition ed where entryId = 38381);

select * from inflectedform where formNoAccent like "merg";
select * from inflectedform where lexemeId = 33808;

# Get the lexeme from DB
select * from inflectedform where formNoAccent like "mănânci" LIMIT 5;
# Get all the conjugations (inflected forms) of the word
select * from inflectedform where lexemeId = (select i.lexemeId from inflectedform i where formNoAccent like "mănânci" LIMIT 1);
# For a conjugation (inflected form), get its inflection
select * from inflection where id = 65;
# Get all inflections of the lexeme
select * from inflection where id in (select i2.inflectionId from inflectedform i2 where lexemeId = (select i.lexemeId from inflectedform i where formNoAccent like "mănânci" LIMIT 1));


select * from inflection where id in (select i.inflectionId from inflectedform i where formNoAccent like "obișnuiești");
