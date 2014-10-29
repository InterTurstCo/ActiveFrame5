SELECT
  "module",
  "module_type",
  "parent_id",
  "parent_id_type",
  "parent_cd",
  "id",
  "id_type",
  "created_date"
FROM ((SELECT
         rkk."module"                  "module",
         rkk."module_type"             "module_type",
         CASE WHEN resf."hierparent" IS NOT NULL THEN resf."hierparent"
         ELSE resf."hierroot" END      "parent_id",
         CASE WHEN resf."hierparent" IS NOT NULL THEN resf."hierparent_type"
         ELSE resf."hierroot_type" END "parent_id_type",
         CASE WHEN resf."hierparent" IS NOT NULL THEN res2."created_date"
         ELSE rkk."created_date" END   "parent_cd",
         res."id",
         res."id_type",
         res."created_date"
       FROM "f_dp_resolution" resf JOIN "f_dp_resltnbase" res ON res."id" = resf."id"
         LEFT JOIN "f_dp_resltnbase" res2 ON res2."id" = resf."hierparent"
         LEFT JOIN "f_dp_rkkbase" rkk ON rkk."id" = resf."hierroot")
      UNION (SELECT
               rep."module"                 "module",
               rep."module_type"            "module_type",
               CASE WHEN rep."hierparent" IS NOT NULL THEN rep."hierparent"
               ELSE rep."hierroot" END      "parent_id",
               CASE WHEN rep."hierparent" IS NOT NULL THEN rep."hierparent_type"
               ELSE rep."hierroot_type" END "parent_id_type",
               CASE WHEN rep."hierparent" IS NOT NULL THEN rep2."created_date"
               ELSE rep."created_date" END  "parent_cd",
               rep."id",
               rep."id_type",
               rep."created_date"
             FROM "f_dp_report" rep LEFT JOIN "f_dp_rkk" rkk ON rkk."id" = rep."hierroot"
               LEFT JOIN "f_dp_report" rep2 ON rep2."id" = rep."hierparent")) s
WHERE 1 = 1 AND "module" = 7 AND "module_type" = 1015 AND "parent_id" = 1087 AND
      "parent_id_type" = 1087_type ORDER BY "parent_id", "parent_cd"