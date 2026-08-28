package com.example.data.db

import com.example.data.dao.OverhaulDao
import com.example.data.entity.*

object GhadirNeyrizDataSeeder {

    suspend fun seedGhadirNeyrizOverhaul(dao: OverhaulDao) {
        // 1. کاربران و ساختار سازمانی مجتمع فولاد غدیر نی‌ریز بر اساس چارت اورهال اعلام شده
        val users = listOf(
            // کاربر ارشد و توسعه‌دهنده سیستم (Super Admin / Developer)
            UserEntity(
                id = 999,
                username = "admin",
                password = "AdMiN",
                name = "توسعه‌دهنده و مدیر ارشد سیستم (Super Admin)",
                email = "developer.admin@ghadirsteel.ir",
                role = "admin",
                unit = "مدیریت جامع اورهال",
                siteId = "GHADIR_NEYRIZ"
            ),
            // مدیر ارشد پروژه اورهال
            UserEntity(
                id = 1,
                username = "aamali",
                password = "123",
                name = "مهندس اعمالی",
                email = "aemali.pm@ghadirsteel.ir",
                role = "admin",
                unit = "مدیریت اورهال",
                siteId = "GHADIR_NEYRIZ"
            ),
            // واحد برنامه‌ریزی تعمیرات
            UserEntity(
                id = 2,
                username = "tajbakhsh",
                password = "123",
                name = "مهندس تاج بخش",
                email = "tajbakhsh.plan@ghadirsteel.ir",
                role = "planner",
                unit = "برنامه‌ریزی تعمیرات",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 3,
                username = "pourkhandani",
                password = "123",
                name = "مهندس پورخندانی",
                email = "pourkhandani.plan@ghadirsteel.ir",
                role = "planner",
                unit = "برنامه‌ریزی تعمیرات",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 4,
                username = "allahbakhshi_plan",
                password = "123",
                name = "مهندس اله بخشی",
                email = "allahbakhshi.plan@ghadirsteel.ir",
                role = "planner",
                unit = "برنامه‌ریزی تعمیرات",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 5,
                username = "shabestan",
                password = "123",
                name = "مهندس شبستان",
                email = "shabestan.plan@ghadirsteel.ir",
                role = "planner",
                unit = "برنامه‌ریزی تعمیرات",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد تعمیرات مکانیک
            UserEntity(
                id = 6,
                username = "allahbakhsh_mech",
                password = "123",
                name = "مهندس اله بخش",
                email = "allahbakhsh.mech@ghadirsteel.ir",
                role = "unit_head",
                unit = "مکانیک",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 7,
                username = "baghari",
                password = "123",
                name = "مهندس بقری",
                email = "baghari.mech@ghadirsteel.ir",
                role = "supervisor",
                unit = "مکانیک",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 8,
                username = "farrokh",
                password = "123",
                name = "مهندس فرخ",
                email = "farrokh.mech@ghadirsteel.ir",
                role = "supervisor",
                unit = "مکانیک",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 9,
                username = "shojaeifard",
                password = "123",
                name = "مهندس شجاعی فرد",
                email = "shojaeifard.mech@ghadirsteel.ir",
                role = "supervisor",
                unit = "مکانیک",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 10,
                username = "mobaraki",
                password = "123",
                name = "مهندس مبارکی",
                email = "mobaraki.mech@ghadirsteel.ir",
                role = "supervisor",
                unit = "مکانیک",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد تعمیرات برق
            UserEntity(
                id = 11,
                username = "yadegar",
                password = "123",
                name = "مهندس یادگار",
                email = "yadegar.elec@ghadirsteel.ir",
                role = "unit_head",
                unit = "برق",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 12,
                username = "alborzi",
                password = "123",
                name = "مهندس البرزی",
                email = "alborzi.elec@ghadirsteel.ir",
                role = "supervisor",
                unit = "برق",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد تعمیرات ابزاردقیق و اتوماسیون (یکپارچه)
            UserEntity(
                id = 13,
                username = "reyhani",
                password = "123",
                name = "مهندس ریحانی",
                email = "reyhani.inst@ghadirsteel.ir",
                role = "unit_head",
                unit = "ابزاردقیق و اتوماسیون",
                siteId = "GHADIR_NEYRIZ"
            ),
            UserEntity(
                id = 14,
                username = "damiri",
                password = "123",
                name = "مهندس دمیری",
                email = "damiri.inst@ghadirsteel.ir",
                role = "supervisor",
                unit = "ابزاردقیق و اتوماسیون",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد تعمیرات نسوز
            UserEntity(
                id = 15,
                username = "yarahmadi",
                password = "123",
                name = "مهندس یاراحمدی",
                email = "yarahmadi.ref@ghadirsteel.ir",
                role = "unit_head",
                unit = "نسوز",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد انرژی و سیالات (WTP / تامین و توزیع آب و گاز)
            UserEntity(
                id = 16,
                username = "zeyghami",
                password = "123",
                name = "مهندس ضیغمی",
                email = "zeyghami.util@ghadirsteel.ir",
                role = "unit_head",
                unit = "انرژی و سیالات",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد بازرسی فنی
            UserEntity(
                id = 17,
                username = "khaki",
                password = "123",
                name = "مهندس خاکی",
                email = "khaki.qc@ghadirsteel.ir",
                role = "unit_head",
                unit = "بازرسی فنی",
                siteId = "GHADIR_NEYRIZ"
            ),

            // واحد ایمنی، بهداشت و محیط زیست (HSE)
            UserEntity(
                id = 18,
                username = "rezaei_hse",
                password = "123",
                name = "مهندس رضایی (سرپرست ایمنی و بهداشت HSE)",
                email = "rezaei.hse@ghadirsteel.ir",
                role = "hse",
                unit = "ایمنی و بهداشت (HSE)",
                siteId = "GHADIR_NEYRIZ"
            )
        )
        dao.insertUsers(users)

        // 2. برنامه جامع اورهال کارخانه احیای مستقیم فولاد غدیر نی‌ریز
        val overhaulProject = OversightEntity(
            id = 1,
            title = "پروژه جامع اورهال سالیانه کارخانه احیای مستقیم فولاد غدیر نی‌ریز (۱۴۰۴)",
            year = 1404,
            equipmentType = "مدول احیای آهن اسفنجی ۸۰۰ هزار تنی با تکنولوژی PERED / Midrex",
            plannedStartDate = "1404/10/12",
            plannedEndDate = "1404/10/29",
            actualStartDate = "1404/10/12",
            status = "active",
            siteId = "GHADIR_NEYRIZ"
        )
        dao.insertOversight(overhaulProject)

        // 3. آیتم‌های ساختار شکست کار (WBS Items) سازمان‌یافته با نواحی Core Area، MHU و WTP
        val items = getGhadirNeyrizOverhaulItems()
        dao.insertItems(items)

        // 4. تخصیص ناظران به فعالیت‌های WBS طبق ساختار تفکیک وظایف
        val assignments = mutableListOf<ItemAssignmentEntity>()
        items.forEach { item ->
            when (item.executiveUnit) {
                "مکانیک" -> {
                    // تخصیص هدفمند بر اساس ناحیه و تجهیز طبق دستورالعمل:
                    // ۱. ناحیه MHU (انتقال مواد و نوارنقاله) -> مهندس بقری (id = 7)
                    // ۲. ناحیه کوره و Core Area -> مهندس شجاعی فرد (id = 9)
                    // ۳. کمپرسورها و تجهیزات دوار -> مهندس فرخ (id = 8)
                    // ۴. سایر نواحی و تجهیزات مکانیک -> مهندس مبارکی (id = 10)
                    val supId = when {
                        item.generalArea.equals("MHU", ignoreCase = true) || item.title.contains("MHU") || item.title.contains("نوار") || item.equipmentName.contains("نوار") -> 7L // مهندس بقری
                        item.equipmentName.contains("کمپرسور") || item.title.contains("کمپرسور") || item.equipmentName.contains("بلور") || item.title.contains("بلوور") -> 8L // مهندس فرخ
                        item.generalArea.equals("Core Area", ignoreCase = true) && (item.equipmentName.contains("کوره") || item.title.contains("کوره") || item.equipmentName.contains("راکتور") || item.title.contains("لگ") || item.title.contains("وینچ") || item.title.contains("اسکوئر")) -> 9L // مهندس شجاعی فرد
                        else -> 10L // مهندس مبارکی
                    }
                    assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = supId))
                }
                "برق" -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 12L)) // مهندس البرزی
                "ابزاردقیق", "ابزاردقیق و اتوماسیون" -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 14L)) // مهندس دمیری
                "نسوز" -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 15L)) // مهندس یاراحمدی
                "انرژی و سیالات" -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 16L)) // مهندس ضیغمی
                "بازرسی فنی" -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 17L)) // مهندس خاکی
                else -> assignments.add(ItemAssignmentEntity(itemId = item.id, supervisorUserId = 7L))
            }
        }
        dao.insertAssignments(assignments)

        // 5. پیش‌نیازهای کلیدی فرآیندی (Prerequisites)
        val prerequisites = listOf(
            ItemPrerequisiteEntity(itemId = 7, prerequisiteItemId = 2), // آماده سازی وینچ بعد شروع
            ItemPrerequisiteEntity(itemId = 22, prerequisiteItemId = 2), // تخلیه کوره بعد شروع
            ItemPrerequisiteEntity(itemId = 23, prerequisiteItemId = 22), // بازکردن منهول کوره بعد تخلیه
            ItemPrerequisiteEntity(itemId = 24, prerequisiteItemId = 23), // فن منهول
            ItemPrerequisiteEntity(itemId = 25, prerequisiteItemId = 23), // سرد شدن کوره
            ItemPrerequisiteEntity(itemId = 27, prerequisiteItemId = 25), // بستن منهول جهت شارژ
            ItemPrerequisiteEntity(itemId = 28, prerequisiteItemId = 27), // شارژ گندله
            ItemPrerequisiteEntity(itemId = 30, prerequisiteItemId = 28), // تعویض لگ داخلی
            ItemPrerequisiteEntity(itemId = 31, prerequisiteItemId = 30), // نسوز کوره
            ItemPrerequisiteEntity(itemId = 32, prerequisiteItemId = 30), // وال اسکیل
            ItemPrerequisiteEntity(itemId = 39, prerequisiteItemId = 32), // تخلیه گندله باقیمانده
            ItemPrerequisiteEntity(itemId = 42, prerequisiteItemId = 39), // تعویض اسکوئر بار
            ItemPrerequisiteEntity(itemId = 43, prerequisiteItemId = 42), // تعویض سکتور پلیت
            ItemPrerequisiteEntity(itemId = 49, prerequisiteItemId = 43), // بستن منهول کوره
            ItemPrerequisiteEntity(itemId = 50, prerequisiteItemId = 49), // شارژ نهایی
            ItemPrerequisiteEntity(itemId = 51, prerequisiteItemId = 50), // تست نشتی
            ItemPrerequisiteEntity(itemId = 53, prerequisiteItemId = 51), // هیت آپ کوره
            ItemPrerequisiteEntity(itemId = 54, prerequisiteItemId = 53), // تزریق گاز
            ItemPrerequisiteEntity(itemId = 55, prerequisiteItemId = 54), // تولید محصول
            ItemPrerequisiteEntity(itemId = 259, prerequisiteItemId = 258), // اورهال کمپرسور پروسس
            ItemPrerequisiteEntity(itemId = 260, prerequisiteItemId = 259), // تست کمپرسور پروسس
            ItemPrerequisiteEntity(itemId = 289, prerequisiteItemId = 288), // اورهال کمپرسور کولینگ
            ItemPrerequisiteEntity(itemId = 317, prerequisiteItemId = 316), // اورهال کمپرسور سیل
            ItemPrerequisiteEntity(itemId = 574, prerequisiteItemId = 557), // تعویض مبدل هوای رکوپراتور
            ItemPrerequisiteEntity(itemId = 1035, prerequisiteItemId = 1034), // تخلیه لجن کلاریفایر
            ItemPrerequisiteEntity(itemId = 1046, prerequisiteItemId = 1035) // پاروهای کلاریفایر
        )
        dao.insertPrerequisites(prerequisites)

        // 6. لاگ‌های کارکرد روزانه نمونه ثبت شده توسط ناظران
        val sampleDailyLogs = listOf(
            DailyWorkLogEntity(
                id = 1,
                itemId = 22,
                oversightId = 1,
                date = "1404/10/12",
                progressPercentage = 100,
                manpowerCount = 6,
                hoursSpent = 12.0,
                recordedByUserId = 7,
                recordedByUserName = "مهندس بقری (ناظر مکانیک)",
                unitName = "مکانیک",
                remarks = "تخلیه کوره با همکاری واحد تولید بدون حادثه کامل شد.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 2,
                itemId = 23,
                oversightId = 1,
                date = "1404/10/12",
                progressPercentage = 100,
                manpowerCount = 4,
                hoursSpent = 5.0,
                recordedByUserId = 8,
                recordedByUserName = "مهندس فرخ (ناظر مکانیک)",
                unitName = "مکانیک",
                remarks = "منهول‌های بیضی، چایناهت و ال.بی.اف با تایید ایمنی باز شدند.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 3,
                itemId = 258,
                oversightId = 1,
                date = "1404/10/12",
                progressPercentage = 100,
                manpowerCount = 5,
                hoursSpent = 6.0,
                recordedByUserId = 9,
                recordedByUserName = "مهندس شجاعی فرد (ناظر مکانیک)",
                unitName = "مکانیک",
                remarks = "سقف کمپرسور پروسس برداشته شد و الاینمنت اولیه بررسی گردید.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 4,
                itemId = 259,
                oversightId = 1,
                date = "1404/10/13",
                progressPercentage = 45,
                manpowerCount = 8,
                hoursSpent = 10.0,
                recordedByUserId = 10,
                recordedByUserName = "مهندس مبارکی (ناظر مکانیک)",
                unitName = "مکانیک",
                remarks = "دمونتاژ پینیون شفت و ایمپلر در حال انجام است.",
                syncedToMsp = false
            ),
            DailyWorkLogEntity(
                id = 5,
                itemId = 1142,
                oversightId = 1,
                date = "1404/10/13",
                progressPercentage = 100,
                manpowerCount = 3,
                hoursSpent = 3.0,
                recordedByUserId = 14,
                recordedByUserName = "مهندس دمیری (ناظر ابزاردقیق و اتوماسیون)",
                unitName = "ابزاردقیق و اتوماسیون",
                remarks = "جابجایی و پایش سورس رادیواکتیو طبق دستورالعمل با موفقیت انجام شد.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 6,
                itemId = 1105,
                oversightId = 1,
                date = "1404/10/12",
                progressPercentage = 100,
                manpowerCount = 4,
                hoursSpent = 4.0,
                recordedByUserId = 15,
                recordedByUserName = "مهندس یاراحمدی (سرپرست نسوز)",
                unitName = "نسوز",
                remarks = "منهول‌های داکت باستل جهت داربست‌بندی باز شدند.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 7,
                itemId = 1605,
                oversightId = 1,
                date = "1404/10/12",
                progressPercentage = 100,
                manpowerCount = 3,
                hoursSpent = 5.0,
                recordedByUserId = 12,
                recordedByUserName = "مهندس البرزی (ناظر برق)",
                unitName = "برق",
                remarks = "تامین روشنایی موقت ۲۴ ولت درون کوره و داکت‌ها مستقر شد.",
                syncedToMsp = true
            ),
            DailyWorkLogEntity(
                id = 8,
                itemId = 1034,
                oversightId = 1,
                date = "1404/10/13",
                progressPercentage = 100,
                manpowerCount = 4,
                hoursSpent = 36.0,
                recordedByUserId = 16,
                recordedByUserName = "مهندس ضیغمی (ناظر سیالات و WTP)",
                unitName = "انرژی و سیالات",
                remarks = "تخلیه آب کلاریفایر و آماده‌سازی برای لجن‌زدایی صورت پذیرفت.",
                syncedToMsp = true
            )
        )
        sampleDailyLogs.forEach { dao.insertDailyWorkLog(it) }

        // 7. جلسه هماهنگی اورهال روزانه
        val session1 = PlanningSessionEntity(
            id = 1,
            oversightId = 1,
            title = "صورتجلسه هماهنگی روزانه اورهال شماره ۱ (بررسی پیشرفت Core Area، کمپرسورها، کوره و WTP)",
            sessionDate = "1404/10/13",
            location = "اتاق جلسات مدیریت اورهال - فولاد غدیر نی‌ریز",
            minutesSummary = "حاضرین: مهندس اعمالی (مدیر پروژه)، مهندس تاج بخش (رئیس برنامه‌ریزی)، مهندسان پورخندانی، اله بخشی و شبستان (برنامه‌ریزی)، مهندس اله بخش (مکانیک)، مهندس یادگار (برق)، مهندس ریحانی (ابزاردقیق)، مهندس یاراحمدی (نسوز)، مهندس ضیغمی (سیالات/WTP)، مهندس خاکی (بازرسی).\nدستور جلسه: ۱. بررسی پیشرفت فیزیکی واقعی نسبت به برنامه در Core Area و WTP ۲. هماهنگی اتمام دمونتاژ شارژ هاپر و تعویض لگ دوتیکه ۳. تسریع لجن‌زدایی کلاریفایر ۴. پیگیری تامین بیرینگ‌های پینیون شفت کمپرسور گاز پروسس.",
            siteId = "GHADIR_NEYRIZ"
        )
        dao.insertSession(session1)

        val decisions = listOf(
            SessionDecisionEntity(
                id = 1,
                sessionId = 1,
                itemId = 17,
                decisionText = "تامین ۴ نفر نیروی کمکی جوشکار به واحد مکانیک برای تسریع در تعویض لگ‌های شارژ هاپر کوره",
                assignedUnit = "مکانیک",
                status = "implemented",
                createdAt = "1404/10/13"
            ),
            SessionDecisionEntity(
                id = 2,
                sessionId = 1,
                itemId = 1142,
                decisionText = "پایش مستمر سورس رادیواکتیو در ناحیه کوره با نظارت حفاظت پرتوی",
                assignedUnit = "ابزاردقیق و اتوماسیون",
                status = "implemented",
                createdAt = "1404/10/13"
            ),
            SessionDecisionEntity(
                id = 3,
                sessionId = 1,
                itemId = 1046,
                decisionText = "تحویل پکینگ‌ها و رابرهای ضدسایش کلاریفایر از انبار به پیمانکار WTP",
                assignedUnit = "انرژی و سیالات",
                status = "implemented",
                createdAt = "1404/10/13"
            ),
            SessionDecisionEntity(
                id = 4,
                sessionId = 1,
                itemId = 478,
                decisionText = "بازرسی NDT و ضخامت‌سنجی تیوب‌ها و فلکسیبل‌های ریفرمر قبل از نصب کاتالیست جدید",
                assignedUnit = "بازرسی فنی",
                status = "pending",
                createdAt = "1404/10/13"
            )
        )
        decisions.forEach { dao.insertSessionDecision(it) }

        // 8. اقلام تدارکات و درخواست‌های تامین قطعات بحرانی
        val procurementItems = listOf(
            ProcurementRequestEntity(
                id = 1,
                itemId = 259,
                sessionId = 1,
                title = "مجموعه بیرینگ ژورنال و تراست کمپرسور گاز پروسس (Demag/Atlas Copco)",
                itemType = "equipment",
                quantity = "۲ ست",
                estimatedCost = "تامین اضطراری اورهال",
                status = "received",
                requestedByUserId = 6,
                requestedByUserName = "مهندس اله بخش",
                approvedByUserId = 1,
                approvedByUserName = "مهندس اعمالی",
                createdAt = "1404/10/05"
            ),
            ProcurementRequestEntity(
                id = 2,
                itemId = 17,
                sessionId = 1,
                title = "قسمت میانی شارژ هاپر کوره احیا با ورق ضد سایش Hardox 500",
                itemType = "equipment",
                quantity = "۱ دست کامل",
                estimatedCost = "موجود در محوطه دپو",
                status = "received",
                requestedByUserId = 6,
                requestedByUserName = "مهندس اله بخش",
                approvedByUserId = 1,
                approvedByUserName = "مهندس اعمالی",
                createdAt = "1404/10/01"
            ),
            ProcurementRequestEntity(
                id = 3,
                itemId = 1046,
                sessionId = 1,
                title = "ست کامل رابرهای ضدسایش پاروهای مکانیزم کلاریفایر WTP",
                itemType = "goods",
                quantity = "۲۴ تیغه",
                estimatedCost = "تحویل انبار WTP",
                status = "received",
                requestedByUserId = 16,
                requestedByUserName = "مهندس ضیغمی",
                approvedByUserId = 1,
                approvedByUserName = "مهندس اعمالی",
                createdAt = "1404/10/08"
            ),
            ProcurementRequestEntity(
                id = 4,
                itemId = 1107,
                sessionId = 1,
                title = "جرم ریختنی آلومینایی کم سیمان (LCC) ویژه داکت‌های باستل و تاپ‌گس کوره",
                itemType = "goods",
                quantity = "۴۵ تن",
                estimatedCost = "تحویل انبار مسقف نسوز",
                status = "received",
                requestedByUserId = 15,
                requestedByUserName = "مهندس یاراحمدی",
                approvedByUserId = 1,
                approvedByUserName = "مهندس اعمالی",
                createdAt = "1404/10/02"
            )
        )
        procurementItems.forEach { dao.insertProcurement(it) }

        // 9. پرمیت‌های ایمنی کارگاهی و مجوزهای کار گرم، فضای بسته و LOTO برق
        val samplePermits = listOf(
            SafetyPermitEntity(
                id = 1,
                itemId = 17, // دمونتاژ و تعویض شارژ هاپر کوره
                oversightId = 1,
                permitNumber = "HSE-1404-0101",
                permitType = "کار گرم و کار در ارتفاع (Hot Work & Height)",
                status = "issued",
                executiveUnit = "مکانیک",
                location = "Core Area - بالای کوره احیا",
                equipmentName = "Furnace Charge Hopper",
                issueDate = "1404/10/14",
                validHours = 12,
                issuedByUserId = 18,
                issuedByUserName = "مهندس رضایی (HSE)",
                requiresElectricalLoto = true,
                electricalLotoStatus = "isolated_and_tagged",
                electricalTaggedBy = "مهندس البرزی (ناظر برق)",
                requiresGasTest = true,
                gasTestResult = "O2: 20.9% | CO: 0 ppm | LEL: 0%",
                requiresScaffoldingTag = true,
                fireWatchRequired = true,
                safetyPrecautions = "استقرار کپسول پودر و گاز ۶ کیلویی، نصب توری ایمنی و مهاربند کامل هارنس ۵ نقطه",
                ppeRequirements = "کلاه ایمنی، کفش پنجه فولادی، هارنس دوتایی ضدسقوط، شیلد جوشکاری و ماسک FFP3",
                createdAt = "1404/10/14 07:30"
            ),
            SafetyPermitEntity(
                id = 2,
                itemId = 259, // اورهال مکانیکال کمپرسور گاز پروسس
                oversightId = 1,
                permitNumber = "HSE-1404-0102",
                permitType = "ایزولاسیون مکانیکی و الکتریکی (LOTO & Lockout)",
                status = "issued",
                executiveUnit = "مکانیک",
                location = "Core Area - سالن کمپرسورها",
                equipmentName = "Process Gas Compressor 6.6KV",
                issueDate = "1404/10/14",
                validHours = 24,
                issuedByUserId = 18,
                issuedByUserName = "مهندس رضایی (HSE)",
                requiresElectricalLoto = true,
                electricalLotoStatus = "isolated_and_tagged",
                electricalTaggedBy = "مهندس یادگار (مدیر واحد برق)",
                requiresGasTest = true,
                gasTestResult = "O2: 20.8% | H2S: 0 ppm | LEL: 0%",
                requiresScaffoldingTag = false,
                fireWatchRequired = false,
                safetyPrecautions = "نصب کارت قرمز (Red Card) روی فیدر ۶.۶ کیلوولت و قفل سوییچ سلول ورودی برق اصلی",
                ppeRequirements = "کفش ایمنی عایق، دستکش چرمی ضدبرش، عینک پلی‌کربنات",
                createdAt = "1404/10/14 08:00"
            ),
            SafetyPermitEntity(
                id = 3,
                itemId = 1035, // تخلیه لجن و اورهال کلاریفایر WTP
                oversightId = 1,
                permitNumber = "HSE-1404-0103",
                permitType = "فضای بسته (Confined Space Entry)",
                status = "issued",
                executiveUnit = "انرژی و سیالات",
                location = "WTP - تصفیه‌خانه آب و کلاریفایر",
                equipmentName = "Clarifier Basin",
                issueDate = "1404/10/14",
                validHours = 8,
                issuedByUserId = 18,
                issuedByUserName = "مهندس رضایی (HSE)",
                requiresElectricalLoto = true,
                electricalLotoStatus = "isolated_and_tagged",
                electricalTaggedBy = "مهندس البرزی (ناظر برق)",
                requiresGasTest = true,
                gasTestResult = "O2: 20.9% | CO: 0 ppm | H2S: 0 ppm",
                requiresScaffoldingTag = false,
                fireWatchRequired = false,
                safetyPrecautions = "برقراری تهویه مداوم با بلوور دمش هوا و حضور نفر مراقب بیرون منهول",
                ppeRequirements = "چکمه ضداسید، ماسک تنفسی کارتریج‌دار، کلاه ایمنی و چراغ قوه ضدجرقه EX",
                createdAt = "1404/10/14 08:30"
            ),
            SafetyPermitEntity(
                id = 4,
                itemId = 1107, // نسوزکاری و آجرچینی داخل کوره
                oversightId = 1,
                permitNumber = "HSE-1404-0104",
                permitType = "فضای بسته و کار در ارتفاع (Confined Space & Height)",
                status = "issued",
                executiveUnit = "نسوز",
                location = "Core Area - داخل راکتور احیا",
                equipmentName = "Reduction Furnace Interior",
                issueDate = "1404/10/14",
                validHours = 12,
                issuedByUserId = 18,
                issuedByUserName = "مهندس رضایی (HSE)",
                requiresElectricalLoto = true,
                electricalLotoStatus = "isolated_and_tagged",
                electricalTaggedBy = "مهندس البرزی (ناظر برق)",
                requiresGasTest = true,
                gasTestResult = "O2: 20.9% | CO: 0 ppm",
                requiresScaffoldingTag = true,
                fireWatchRequired = false,
                safetyPrecautions = "تایید تگ سبز داربست‌بندی طبقاتی، کنترل روشنایی ولتاژ پایین ۲۴ ولت",
                ppeRequirements = "لباس کار یکسره، ماسک گرد و غبار P3، هارنس ایمنی، کلاه چانه‌دار",
                createdAt = "1404/10/14 09:00"
            ),
            SafetyPermitEntity(
                id = 5,
                itemId = 1684, // تست و سرویس سوئیچ‌گیر 6.6KV
                oversightId = 1,
                permitNumber = "HSE-1404-0105",
                permitType = "ایزولاسیون برقی و ولتاژ بالا (High Voltage Isolation)",
                status = "pending",
                executiveUnit = "برق",
                location = "Core Area - پست برق ۳۳/۶.۶ کیلوولت",
                equipmentName = "SWG 6.6-M10 & M20",
                issueDate = "1404/10/14",
                validHours = 8,
                issuedByUserId = 18,
                issuedByUserName = "مهندس رضایی (HSE)",
                requiresElectricalLoto = true,
                electricalLotoStatus = "pending_isolation",
                electricalTaggedBy = "",
                requiresGasTest = false,
                gasTestResult = "",
                requiresScaffoldingTag = false,
                fireWatchRequired = false,
                safetyPrecautions = "قطع خط ورودی، تخلیه بار خازنی با ارتینگ ایمن و نصب علائم هشدار خطر برق‌گرفتگی و کارت قرمز",
                ppeRequirements = "دستکش عایق ۲۰ کیلوولت، شیلد ضد آرک‌فلش، لباس نسوز ضدجرقه",
                createdAt = "1404/10/14 09:30"
            )
        )
        samplePermits.forEach { dao.insertSafetyPermit(it) }
    }

    private fun getGhadirNeyrizOverhaulItems(): List<OversightItemEntity> {
        val list = mutableListOf<OversightItemEntity>()

        fun add(
            id: Long,
            wbsCode: String,
            title: String,
            parentItemId: Long?,
            outlineLevel: Int,
            unit: String,
            area: String,
            loc: String,
            equip: String,
            duration: Double,
            startDate: String,
            endDate: String,
            status: String = "pending",
            progress: Int = 0,
            manpower: Int = 0,
            actualHours: Double = 0.0,
            notes: String = ""
        ) {
            // یکپارچه‌سازی واحد اتوماسیون و ابزاردقیق
            val normalizedUnit = if (unit == "اتوماسیون" || unit == "ابزار دقیق") "ابزاردقیق و اتوماسیون" else unit
            
            // نرمال‌سازی سه ناحیه اصلی: Core Area، MHU و WTP
            val normalizedArea = when {
                area.contains("Water", ignoreCase = true) || area.contains("WTP", ignoreCase = true) || area.contains("سیالات") -> "WTP"
                area.contains("MHU", ignoreCase = true) || area.contains("مواد") -> "MHU"
                area.contains("Blower", ignoreCase = true) || area.contains("Reformer", ignoreCase = true) || area.contains("Core", ignoreCase = true) || area.contains("Furnace", ignoreCase = true) -> "Core Area"
                else -> area
            }

            list.add(
                OversightItemEntity(
                    id = id,
                    oversightId = 1,
                    wbsCode = wbsCode,
                    title = title,
                    parentItemId = parentItemId,
                    outlineLevel = outlineLevel,
                    executiveUnit = normalizedUnit,
                    generalArea = normalizedArea,
                    executionLocation = loc,
                    equipmentName = equip,
                    durationHours = duration,
                    actualHours = actualHours,
                    manpowerCount = manpower,
                    status = status,
                    progressPercentage = progress,
                    plannedStartDate = startDate,
                    plannedEndDate = endDate,
                    actualStartDate = if (progress > 0) startDate else null,
                    actualEndDate = if (progress == 100) endDate else null,
                    notes = notes
                )
            )
        }

        // 1. Overhaul Root
        add(1, "1", "اورهال جامع سالیانه کارخانه احیای مستقیم فولاد غدیر نی‌ریز", null, 1, "مدیریت اورهال", "Core Area", "کل سایت", "مدول احیای آهن اسفنجی", 180.0, "1404/10/12", "1404/10/29", "in_progress", 34)
        add(2, "1.1", "شروع رسمی اورهال و صدور پرمیت‌های ایمنی (LOTO)", 1, 2, "ایمنی و بهره‌برداری", "Core Area", "کل سایت", "پرمیت‌ها", 0.0, "1404/10/12", "1404/10/12", "completed", 100, 4, 2.0)

        // ==========================================
        // 2. MECHANICAL UNIT (واحد مکانیک)
        // ==========================================
        add(3, "2", "واحد مکانیک (Mechanical Maintenance)", 1, 2, "مکانیک", "Core Area", "سایت احیا", "تجهیزات مکانیکی", 180.0, "1404/10/12", "1404/10/29", "in_progress", 38)
        
        // 2.1 Core Area - Mechanical
        add(4, "2.1", "Core Area (ناحیه کوره و هسته مرکزی فرآیند)", 3, 3, "مکانیک", "Core Area", "کوره و سازه", "هسته مرکزی", 180.0, "1404/10/12", "1404/10/29", "in_progress", 42)
        
        // Furnace Area
        add(5, "2.1.1", "Furnace Area (ناحیه کوره احیا)", 4, 4, "مکانیک", "Core Area", "Furnace Area", "کوره احیا", 180.0, "1404/10/12", "1404/10/29", "in_progress", 48)
        
        // Furnace charge hopper
        add(6, "2.1.1.1", "Furnace charge hopper (شارژ هاپر کوره)", 5, 5, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 158.0, "1404/10/12", "1404/10/27", "in_progress", 52)
        add(7, "2.1.1.1.1", "آماده سازی وتست وینج", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 2.0, "1404/10/12", "1404/10/12", "completed", 100, 3, 2.0)
        add(8, "2.1.1.1.2", "پاکسازی شوت ورودی به چارج هاپر از رسوبات جداره داخلی قبل از تخلیه", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 2.0, "1404/10/27", "1404/10/27", "pending", 0)
        add(9, "2.1.1.1.3", "در صورت نیاز ترمیم شوت ورودی به شارژ هاپر همراه با نصب ناودانی", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 6.0, "1404/10/27", "1404/10/27", "pending", 0)
        add(10, "2.1.1.1.4", "پاکسازی رسوبات بین بدنه چارج هاپر و گریتینگ طبقات ضلع شرقی", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 2.0, "1404/10/27", "1404/10/27", "pending", 0)
        add(11, "2.1.1.1.5", "ضخامت سنجی لگ های بیرونی اختاپوسی و در صورت نیاز بازکردن و تعویض آنها", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 15.0, "1404/10/15", "1404/10/16", "in_progress", 60, 4, 9.0)
        add(12, "2.1.1.1.6", "محافظت از ترموول ها، ترانسمیتر و کابل ترموکوپل ها با باکس استوانه ای", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 3.0, "1404/10/13", "1404/10/13", "completed", 100, 2, 3.0)
        add(13, "2.1.1.1.7", "ضخامت سنجی از بدنه چارج هاپر", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 2.0, "1404/10/15", "1404/10/15", "completed", 100, 2, 2.0)
        add(14, "2.1.1.1.8", "دمونتاژ قسمت میانی شارژ هاپر، اسلایدگیت بالا، لگ دو تکه و سیل گس کن", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 25.0, "1404/10/15", "1404/10/17", "in_progress", 70, 6, 17.5)
        add(15, "2.1.1.1.9", "ضخامت سنجی لگ دوتیکه زیر گیت بالای کوره و تعویض در صورت نیاز", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 5.0, "1404/10/17", "1404/10/17", "pending", 0)
        add(16, "2.1.1.1.10", "چک و بررسی از قسمت داخلی لگ دوتیکه زیر گیت بالای کوره", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 2.0, "1404/10/17", "1404/10/17", "pending", 0)
        add(17, "2.1.1.1.11", "تعویض قسمت میانی شارژ هاپر با ورق‌های ضدسایش", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 50.0, "1404/10/17", "1404/10/22", "pending", 0)
        add(18, "2.1.1.1.12", "تعویض سیل گس کن بالای کوره", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 10.0, "1404/10/22", "1404/10/23", "pending", 0)
        add(19, "2.1.1.1.13", "تعویض مجموعه اسلاید گیت بالای کوره", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 10.0, "1404/10/23", "1404/10/24", "pending", 0)
        add(20, "2.1.1.1.14", "باز کردن هدر و فلاشینگ لاین آب فلاشینگ ورودی به شارژ هاپر", 6, 6, "مکانیک", "Core Area", "Furnace Area", "Furnace charge hopper", 6.0, "1404/10/13", "1404/10/13", "completed", 100, 3, 6.0)

        // Reduction furnace - Inside
        add(21, "2.1.1.2", "Reduction furnace - Inside (داخل کوره احیا)", 5, 5, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 180.0, "1404/10/12", "1404/10/29", "in_progress", 35)
        add(22, "2.1.1.2.1", "تخلیه کوره احیا با هماهنگی بهره‌برداری", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 12.0, "1404/10/12", "1404/10/12", "completed", 100, 6, 12.0)
        add(23, "2.1.1.2.2", "باز کردن منهول های کوره (منهول بیضی، چایناهت و LBF)", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 5.0, "1404/10/12", "1404/10/12", "completed", 100, 4, 5.0)
        add(24, "2.1.1.2.3", "نصب فن در محل منهول های کوره جهت تهویه و خنک‌کاری", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 3.0, "1404/10/12", "1404/10/12", "completed", 100, 3, 3.0)
        add(25, "2.1.1.2.4", "سرد شدن کوره (Cool Down Cycle)", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 48.0, "1404/10/12", "1404/10/14", "completed", 100, 2, 48.0)
        add(26, "2.1.1.2.5", "بازرسی از کوره شامل پنجه های UBF و LBF، چایناهت، ترموول ها و فلشینگ نسوز", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 1.0, "1404/10/14", "1404/10/14", "completed", 100, 3, 1.0)
        add(27, "2.1.1.2.6", "بستن منهول های کوره جهت شارژ گندله برای تخریب وال اسکیل", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 4.0, "1404/10/14", "1404/10/14", "completed", 100, 4, 4.0)
        add(28, "2.1.1.2.7", "شارژ گندله درون کوره جهت تخریب وال اسکیل و ترمیم نسوز", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 9.0, "1404/10/14", "1404/10/15", "completed", 100, 5, 9.0)
        add(29, "2.1.1.2.8", "ضخامت سنجی لگ های داخلی کوره", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 2.0, "1404/10/15", "1404/10/15", "completed", 100, 2, 2.0)
        add(30, "2.1.1.2.9", "تعویض لگ های داخلی کوره پس از ضخامت سنجی", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 70.0, "1404/10/15", "1404/10/18", "in_progress", 45, 8, 31.5)
        add(31, "2.1.1.2.10", "تعمیرات نسوز درون کوره تا چاینا هت", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 120.0, "1404/10/18", "1404/10/23", "pending", 0)
        add(32, "2.1.1.2.11", "تخریب وال اسکیل درون کوره تا چاینا هت", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 120.0, "1404/10/18", "1404/10/23", "pending", 0)
        add(39, "2.1.1.2.12", "تخلیه گندله باقیمانده درون کوره", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 8.0, "1404/10/24", "1404/10/24", "pending", 0)
        add(42, "2.1.1.2.13", "تعویض Square Bar های هر چهار کلوخه شکن بالا", 21, 6, "مکانیک", "Core Area", "Furnace Area", "کلوخه شکن کوره", 42.0, "1404/10/24", "1404/10/26", "pending", 0)
        add(43, "2.1.1.2.14", "تعویض Sector Plate های هر چهار کلوخه شکن بالا", 21, 6, "مکانیک", "Core Area", "Furnace Area", "کلوخه شکن کوره", 42.0, "1404/10/24", "1404/10/26", "pending", 0)
        add(49, "2.1.1.2.15", "بستن منهول های کوره و آماده‌سازی شارژ نهایی", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 4.0, "1404/10/26", "1404/10/26", "pending", 0)
        add(50, "2.1.1.2.16", "شارژ گندله درون کوره پس از اتمام تعمیرات داخل و بالای کوره", 21, 6, "مکانیک", "Core Area", "Furnace Area", "داخل کوره", 8.0, "1404/10/26", "1404/10/26", "pending", 0)
        add(51, "2.1.1.2.17", "تست نشتی هلیوم و فشار کوره و تجهیزات", 21, 6, "مکانیک", "Core Area", "Furnace Area", "تجهیزات کوره", 10.0, "1404/10/26", "1404/10/27", "pending", 0)
        add(53, "2.1.1.2.18", "هیت آپ کوره (Heat Up)", 21, 6, "مکانیک", "Core Area", "Furnace Area", "کوره احیا", 30.0, "1404/10/27", "1404/10/29", "pending", 0)
        add(54, "2.1.1.2.19", "تزریق گاز فرآیندی به راکتور احیا", 21, 6, "مکانیک", "Core Area", "Furnace Area", "کوره احیا", 8.0, "1404/10/29", "1404/10/29", "pending", 0)
        add(55, "2.1.1.2.20", "تولید محصول آهن اسفنجی (DRI Cold & Hot Discharge)", 21, 6, "مکانیک", "Core Area", "Furnace Area", "کوره احیا", 0.0, "1404/10/29", "1404/10/29", "pending", 0)

        // Top gas scrubber & Cooling gas scrubber (در Core Area)
        add(91, "2.1.1.3", "Top gas scrubber (اسکرابر گاز تاپ)", 5, 5, "مکانیک", "Core Area", "Furnace Area", "Top gas scrubber", 123.0, "1404/10/12", "1404/10/24", "in_progress", 40)
        add(92, "2.1.1.3.1", "نصب شوت جهت تخلیه پکینگ های اسکرابر تاپ گس", 91, 6, "مکانیک", "Core Area", "Furnace Area", "Top gas scrubber", 2.0, "1404/10/12", "1404/10/12", "completed", 100, 3, 2.0)
        add(107, "2.1.1.3.2", "تخلیه پکینگ های تاپ گس", 91, 6, "مکانیک", "Core Area", "Furnace Area", "Top gas scrubber", 40.0, "1404/10/13", "1404/10/17", "in_progress", 70, 5, 28.0)
        add(127, "2.1.1.3.3", "تعویض کامل ونچوری اسکرابر تاپ گس", 91, 6, "مکانیک", "Core Area", "Furnace Area", "Top gas scrubber", 30.0, "1404/10/18", "1404/10/21", "pending", 0)
        add(130, "2.1.1.3.4", "شارژ پکینگ های جدید اسکرابر تاپ گس", 91, 6, "مکانیک", "Core Area", "Furnace Area", "Top gas scrubber", 20.0, "1404/10/21", "1404/10/23", "pending", 0)

        add(142, "2.1.1.4", "Cooling gas scrubber (اسکرابر گاز کولینگ)", 5, 5, "مکانیک", "Core Area", "Furnace Area", "Cooling gas scrubber", 117.0, "1404/10/12", "1404/10/23", "in_progress", 35)
        add(160, "2.1.1.4.1", "تخلیه پکینگ های اسکرابر کولینگ", 142, 6, "مکانیک", "Core Area", "Furnace Area", "Cooling gas scrubber", 30.0, "1404/10/13", "1404/10/16", "in_progress", 60, 4, 18.0)
        add(179, "2.1.1.4.2", "شارژ پکینگ های جدید اسکرابر کولینگ", 142, 6, "مکانیک", "Core Area", "Furnace Area", "Cooling gas scrubber", 12.0, "1404/10/19", "1404/10/20", "pending", 0)

        // Core Area -> Blower & Compressors
        add(256, "2.2", "Blower & Compressor Section (کمپرسورهای فرآیندی در Core Area)", 3, 3, "مکانیک", "Core Area", "Blower Room", "کمپرسورهای فرآیندی", 146.0, "1404/10/12", "1404/10/26", "in_progress", 42)
        add(257, "2.2.1", "Process Gas Compressor (کمپرسور گاز پروسس)", 256, 4, "مکانیک", "Core Area", "Blower Room", "Process Gas Compressor", 138.0, "1404/10/12", "1404/10/25", "in_progress", 52)
        add(258, "2.2.1.1", "باز کردن سقف کمپرسور پروسس", 257, 5, "مکانیک", "Core Area", "Blower Room", "Process Gas Compressor", 6.0, "1404/10/12", "1404/10/12", "completed", 100, 5, 6.0)
        add(259, "2.2.1.2", "اورهال کمپرسور پروسس، نصب روتور، بولگیر، پینیون شفت و بیرینگ‌ها", 257, 5, "مکانیک", "Core Area", "Blower Room", "Process Gas Compressor", 50.0, "1404/10/12", "1404/10/17", "in_progress", 50, 8, 25.0)
        add(260, "2.2.1.3", "استارت کمپرسور پروسس و انجام تست های عملکردی و الاینمنت لیزری", 257, 5, "مکانیک", "Core Area", "Blower Room", "Process Gas Compressor", 30.0, "1404/10/19", "1404/10/21", "pending", 0)
        add(261, "2.2.1.4", "تعویض روتور، پینیون شفت و سیلینگ‌های جدید با قطعات اصلی", 257, 5, "مکانیک", "Core Area", "Blower Room", "Process Gas Compressor", 34.0, "1404/10/22", "1404/10/25", "pending", 0)

        // Cooling Gas Compressor
        add(287, "2.2.2", "Cooling Gas Compressor (کمپرسور گاز کولینگ)", 256, 4, "مکانیک", "Core Area", "Blower Room", "Cooling Gas Compressor", 137.0, "1404/10/12", "1404/10/25", "in_progress", 38)
        add(288, "2.2.2.1", "باز کردن سقف کمپرسور کولینگ", 287, 5, "مکانیک", "Core Area", "Blower Room", "Cooling Gas Compressor", 4.0, "1404/10/12", "1404/10/12", "completed", 100, 4, 4.0)
        add(289, "2.2.2.2", "اورهال کمپرسور کولینگ و نصب روتور و بیرینگ‌های نو", 287, 5, "مکانیک", "Core Area", "Blower Room", "Cooling Gas Compressor", 50.0, "1404/10/12", "1404/10/17", "in_progress", 35, 6, 17.5)

        // Seal Gas Compressor
        add(315, "2.2.3", "Seal gas Compressor (کمپرسور گاز سیل)", 256, 4, "مکانیک", "Core Area", "Blower Room", "Seal gas Compressor", 105.0, "1404/10/16", "1404/10/26", "pending", 0)
        add(316, "2.2.3.1", "باز کردن سقف کمپرسور سیل", 315, 5, "مکانیک", "Core Area", "Blower Room", "Seal gas Compressor", 4.0, "1404/10/16", "1404/10/16", "pending", 0)
        add(317, "2.2.3.2", "اورهال کمپرسور سیل مطابق با شرح خدمات پیمانکار", 315, 5, "مکانیک", "Core Area", "Blower Room", "Seal gas Compressor", 30.0, "1404/10/16", "1404/10/18", "pending", 0)

        // Core Area -> Reformer Section
        add(473, "2.3", "Reformer & Heat Recovery (ریفرمر و رکوپراتور در Core Area)", 3, 3, "مکانیک", "Core Area", "Reformer Box", "ریفرمر", 130.0, "1404/10/13", "1404/10/25", "in_progress", 25)
        add(478, "2.3.1", "Reformer tubes (تیوب‌های ریفرمر و سیستم تعلیق اسپرینگ)", 473, 4, "مکانیک", "Core Area", "Reformer Box", "Reformer tubes", 130.0, "1404/10/13", "1404/10/25", "in_progress", 30)
        add(480, "2.3.1.1", "باز کردن کپ تعدادی از داگ لگ های ریفرمر بر اساس چک‌لیست", 478, 5, "مکانیک", "Core Area", "Reformer Box", "Reformer tubes", 30.0, "1404/10/16", "1404/10/18", "pending", 0)
        add(482, "2.3.1.2", "تعیین سطح کاتالیست ها و میک‌آپ کاتالیست فعال تیوب‌ها", 478, 5, "مکانیک", "Core Area", "Reformer Box", "Reformer tubes", 10.0, "1404/10/16", "1404/10/16", "pending", 0)
        add(489, "2.3.1.3", "تعویض فلکسیبل های گاز فید ریفرمر", 478, 5, "مکانیک", "Core Area", "Reformer Box", "Reformer tubes", 60.0, "1404/10/14", "1404/10/19", "in_progress", 40, 4, 24.0)
        add(557, "2.3.2", "RECUPERATOR (ریکوپراتور و تیوب باندل‌ها)", 473, 4, "مکانیک", "Core Area", "Recuperator Area", "Recuperator", 150.0, "1404/10/12", "1404/10/26", "in_progress", 25)
        add(574, "2.3.2.1", "تعویض مبدل هوای مرحله دوم شمالی ریکوپراتور", 557, 5, "مکانیک", "Core Area", "Recuperator Area", "Recuperator", 30.0, "1404/10/20", "1404/10/22", "pending", 0)

        // 2.4 Material Handling Area (MHU) - Mechanical
        add(626, "2.4", "Material Handling Area - MHU (ناحیه انتقال مواد و نوارها)", 3, 3, "مکانیک", "MHU", "انتقال مواد", "نوارها و فیدرها", 150.0, "1404/10/12", "1404/10/26", "in_progress", 35)
        add(628, "2.4.1", "ناحیه کامیون ریزها (Truck Unloading)", 626, 4, "مکانیک", "MHU", "Truck Unloading", "فیدرهای ۱ تا ۵", 80.0, "1404/10/12", "1404/10/19", "in_progress", 45)
        add(629, "2.4.1.1", "چک و تعویض ورق های هاردکس کف فیدر های ۱ تا ۵ تراک آنلودینگ", 628, 5, "مکانیک", "MHU", "Truck Unloading", "فیدرهای ۱ تا ۵", 50.0, "1404/10/12", "1404/10/16", "in_progress", 50, 6, 25.0)
        add(642, "2.4.2", "نوار ۴۴۰ و جانکشن صفر", 626, 4, "مکانیک", "MHU", "Junction 0", "نوار ۴۴۰", 79.0, "1404/10/12", "1404/10/19", "in_progress", 40)
        add(659, "2.4.3", "نوار ۴۵۰ و جانکشن شماره یک", 626, 4, "مکانیک", "MHU", "Junction 1", "نوار ۴۵۰", 47.0, "1404/10/12", "1404/10/16", "in_progress", 60)
        add(669, "2.4.4", "نوار ۶۰۵ و جانکشن شماره دو", 626, 4, "مکانیک", "MHU", "Junction 2", "نوار ۶۰۵", 60.0, "1404/10/12", "1404/10/17", "in_progress", 35)
        add(712, "2.4.5", "نوار ۱۳۰۱ (After Day Bin)", 626, 4, "مکانیک", "MHU", "Day Bin Area", "نوار ۱۳۰۱", 118.0, "1404/10/12", "1404/10/23", "in_progress", 30)
        add(729, "2.4.6", "سرند اکساید (Oxide Screen)", 626, 4, "مکانیک", "MHU", "Screening Area", "سرند اکساید", 58.0, "1404/10/15", "1404/10/20", "pending", 0)
        add(777, "2.4.7", "Day Bin (مخزن دی‌بین و تجهیزات زیر مخزن)", 626, 4, "مکانیک", "MHU", "Day Bin Area", "Day Bin", 150.0, "1404/10/12", "1404/10/26", "in_progress", 25)
        add(786, "2.4.7.1", "تعویض رک لدر دی بین (Rack Ladder)", 777, 5, "مکانیک", "MHU", "Day Bin Area", "Day Bin", 120.0, "1404/10/14", "1404/10/26", "pending", 0)

        // 2.5 Water Treatment Plant (WTP) - Mechanical
        add(809, "2.5", "Water Treatment Plant - WTP (تصفیه و توزیع آب و پساب)", 3, 3, "مکانیک", "WTP", "تاسیسات آب WTP", "پمپخانه‌ها و حوضچه‌ها", 158.0, "1404/10/12", "1404/10/27", "in_progress", 36)
        add(810, "2.5.1", "حوضچه‌ها و بیسین‌های آب فرآیندی (Basins & Sumps)", 809, 4, "مکانیک", "WTP", "حوضچه‌ها", "مخازن آب", 75.0, "1404/10/13", "1404/10/21", "in_progress", 35)
        add(811, "2.5.1.1", "حوضچه ماشینری (تخلیه لجن، شستشو و آبگیری)", 810, 5, "مکانیک", "WTP", "حوضچه ماشینری", "مخزن ماشینری", 10.0, "1404/10/16", "1404/10/16", "pending", 0)
        add(817, "2.5.1.2", "حوضچه آب تمیز (Clean Water Basin)", 810, 5, "مکانیک", "WTP", "حوضچه تمیز", "مخزن آب تمیز", 17.0, "1404/10/16", "1404/10/18", "pending", 0)
        add(823, "2.5.1.3", "حوضچه پروسس گرم (Hot Process Water Basin)", 810, 5, "مکانیک", "WTP", "حوضچه گرم", "مخزن آب گرم", 75.0, "1404/10/13", "1404/10/21", "in_progress", 45, 5, 33.7)
        add(839, "2.5.2", "Pump House (پمپ‌خانه مرکزی WTP)", 809, 4, "مکانیک", "WTP", "Pump House", "پمپ‌های فرآیندی", 74.0, "1404/10/14", "1404/10/21", "in_progress", 40)
        add(840, "2.5.2.1", "Clean Water Pumps (پمپ‌های آب تمیز ۱۹.۱ و ۱۹.۲)", 839, 5, "مکانیک", "WTP", "Pump House", "PU-06-19", 25.0, "1404/10/16", "1404/10/18", "pending", 0)
        add(875, "2.5.2.2", "Cold Process Water Pumps (پمپ‌های آب سرد ۱۶.۱ تا ۱۶.۴)", 839, 5, "مکانیک", "WTP", "Pump House", "PU-06-16", 74.0, "1404/10/14", "1404/10/21", "in_progress", 45, 6, 33.0)
        add(986, "2.5.3", "Cooling Tower (برج‌های خنک‌کننده پروسس و کلین)", 809, 4, "مکانیک", "WTP", "Cooling Tower", "کولینگ تاورها", 80.0, "1404/10/14", "1404/10/21", "in_progress", 35)
        add(990, "2.5.3.1", "تعویض پکینگ‌ها و قطره‌گیرهای کولینگ تاور کثیف ۱۳.۱", 986, 5, "مکانیک", "WTP", "Cooling Tower 13.1", "CT-13.1", 70.0, "1404/10/14", "1404/10/20", "in_progress", 40, 6, 28.0)
        add(1018, "2.5.4", "Clarifier (کلاریفایر و مکانیزم ته‌نشینی لجن)", 809, 4, "مکانیک", "WTP", "Clarifier Area", "کلاریفایر", 128.0, "1404/10/13", "1404/10/26", "in_progress", 30)
        add(1034, "2.5.4.1", "تخلیه آب کلاریفایر با هماهنگی واحد تولید", 1018, 5, "مکانیک", "WTP", "Clarifier Area", "کلاریفایر", 36.0, "1404/10/13", "1404/10/15", "completed", 100, 4, 36.0)
        add(1035, "2.5.4.2", "تخلیه لجن و تمیزکاری کف کلاریفایر پس از تخلیه آب", 1018, 5, "مکانیک", "WTP", "Clarifier Area", "کلاریفایر", 40.0, "1404/10/15", "1404/10/18", "in_progress", 45, 6, 18.0)
        add(1046, "2.5.4.3", "تعویض پاروهای کلاریفایر و رابرهای ضد سایش", 1018, 5, "مکانیک", "WTP", "Clarifier Area", "کلاریفایر", 30.0, "1404/10/19", "1404/10/21", "pending", 0)

        // ==========================================
        // 3. REFRACTORY UNIT (واحد نسوز)
        // ==========================================
        add(1092, "3", "واحد نسوز (Refractory Maintenance)", 1, 2, "نسوز", "Core Area", "کوره و داکت‌ها", "نسوزکاری و آجرچینی", 140.0, "1404/10/12", "1404/10/26", "in_progress", 30)
        add(1093, "3.1", "Core Area - Refractory (نسوزکاری کوره و داکت‌های Core Area)", 1092, 3, "نسوز", "Core Area", "کوره احیا", "نسوز کوره", 140.0, "1404/10/12", "1404/10/26", "in_progress", 35)
        add(1095, "3.1.1", "Top Gas Duct (داکت گاز تاپ)", 1093, 4, "نسوز", "Core Area", "Top Gas Duct", "داکت تاپ گس", 137.0, "1404/10/12", "1404/10/26", "in_progress", 40)
        add(1098, "3.1.1.1", "تعمیرات نسوز تاپ گس داکت شامل تخریب نسوز، جرم ریزی، جوشکاری انکر و آجرچینی", 1095, 5, "نسوز", "Core Area", "Top Gas Duct", "داکت تاپ گس", 10.0, "1404/10/15", "1404/10/15", "pending", 0)
        add(1104, "3.2", "Bustle Gas Ducts (داکت‌های گاز باستل)", 1092, 3, "نسوز", "Core Area", "Bustle Line", "داکت باستل", 140.0, "1404/10/12", "1404/10/26", "in_progress", 30)
        add(1105, "3.2.1", "بازکردن منهول های داکت های باستل پس از هماهنگی تولید", 1104, 4, "نسوز", "Core Area", "Bustle Line", "داکت باستل", 4.0, "1404/10/12", "1404/10/12", "completed", 100, 4, 4.0)
        add(1107, "3.2.2", "انجام تعمیرات نسوز داکت های گاز باستل (تخریب، قالب‌بندی، جرم‌ریزی و پتوکاری)", 1104, 4, "نسوز", "Core Area", "Bustle Line", "داکت باستل", 48.0, "1404/10/15", "1404/10/19", "pending", 0)
        add(1126, "3.3", "Flue Gas Ducts (داکت‌های فلوگس ریفرمر)", 1092, 3, "نسوز", "Core Area", "Flue Gas Ducts", "داکت فلوگس", 106.0, "1404/10/12", "1404/10/23", "in_progress", 25)
        add(1128, "3.3.1", "انجام تعمیرات نسوز فلوگس داکت جنوبی (تخریب، جوشکاری انکر و جرم‌ریزی)", 1126, 4, "نسوز", "Core Area", "Flue Gas Ducts", "فلوگس جنوبی", 72.0, "1404/10/13", "1404/10/16", "in_progress", 35, 8, 25.0)
        add(1131, "3.3.2", "انجام تعمیرات نسوز فلوگس داکت شمالی", 1126, 4, "نسوز", "Core Area", "Flue Gas Ducts", "فلوگس شمالی", 40.0, "1404/10/19", "1404/10/23", "pending", 0)

        // ====================================================================
        // 4. INSTRUMENTATION & AUTOMATION UNIT (واحد ابزاردقیق و اتوماسیون یکپارچه)
        // ====================================================================
        add(1137, "4", "واحد ابزاردقیق و اتوماسیون (Instrumentation & Automation)", 1, 2, "ابزاردقیق و اتوماسیون", "Core Area", "سایت و اتاق کنترل", "ترانسمیترها، ولوها، DCS و سیستم‌های کنترل", 145.0, "1404/10/12", "1404/10/26", "in_progress", 36)
        
        // 4.1 Core Area Instrument & Automation
        add(1139, "4.1", "Furnace Area Instrument (ابزار دقیق و کنترل ناحیه کوره)", 1137, 3, "ابزاردقیق و اتوماسیون", "Core Area", "Furnace Area", "سنسورهای کوره", 135.0, "1404/10/13", "1404/10/26", "in_progress", 40)
        add(1141, "4.1.1", "کالیبره لودسل و لول سوئیچ شارژ هاپر قبل از آخرین شارژ کوره", 1139, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Furnace Area", "Charge Hopper", 2.0, "1404/10/26", "1404/10/26", "pending", 0)
        add(1142, "4.1.2", "جابجایی و حفاظت از سورس رادیواکتیو جهت تعویض بدنه شارژ هاپر", 1139, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Furnace Area", "Charge Hopper", 3.0, "1404/10/13", "1404/10/13", "completed", 100, 3, 3.0)
        add(1165, "4.1.3", "FV-X169 چک و بررسی و کالیبره کنترل ولو ناحیه باستل", 1139, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Bustle Line", "Control Valves", 1.5, "1404/10/20", "1404/10/20", "pending", 0)
        
        // 4.2 Compressors Instrument
        add(1229, "4.2", "Blower Area Instrument (ابزار دقیق بلوورها و کمپرسورها)", 1137, 3, "ابزاردقیق و اتوماسیون", "Core Area", "Blower Room", "ابزار دقیق کمپرسورها", 123.0, "1404/10/14", "1404/10/26", "in_progress", 35)
        add(1252, "4.2.1", "FT-X191.1 چک و کالیبره فلومتر پیتوت کمپرسور کولینگ", 1229, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Cooling Compressor", "Flowmeter", 2.0, "1404/10/20", "1404/10/20", "pending", 0)
        add(1268, "4.2.2", "FT-X115.1 چک و کالیبره فلومتر پیتوت کمپرسور پروسس", 1229, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Process Compressor", "Flowmeter", 3.0, "1404/10/21", "1404/10/21", "pending", 0)
        add(1273, "4.2.3", "بررسی سنسورهای ویبره (Vibration Probes Bently Nevada) کمپرسور پروسس", 1229, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Process Compressor", "Vibration Sensors", 2.0, "1404/10/21", "1404/10/21", "pending", 0)
        
        // 4.3 Reformer Instrument
        add(1355, "4.3", "Reformer Instrument & Analyzers (ابزار دقیق ریفرمر و آنالایزرها)", 1137, 3, "ابزاردقیق و اتوماسیون", "Core Area", "Analyzer Room", "آنالایزرهای گاز", 73.0, "1404/10/15", "1404/10/22", "in_progress", 30)
        add(1368, "4.3.1", "کالیبراسیون و پرج آنالایزرهای AIT-X131.1 و 132.1 گاز احیا", 1355, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Analyzer Room", "Gas Analyzers", 1.0, "1404/10/15", "1404/10/15", "completed", 100, 2, 1.0)
        add(1391, "4.3.2", "تعویض ترموکوپل های معیوب کوره و باکس ریفرمر", 1355, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Reformer Box", "Thermocouples", 3.0, "1404/10/22", "1404/10/22", "pending", 0)

        // 4.4 DCS & Automation Systems (اتاق سرور و رک‌های DCS)
        add(1564, "4.4", "Industrial Automation & DCS (سیستم‌های اتوماسیون و DCS)", 1137, 3, "ابزاردقیق و اتوماسیون", "Core Area", "Server Room & Racks", "DCS, PLC & Profibus", 104.0, "1404/10/14", "1404/10/24", "in_progress", 35)
        add(1568, "4.4.1", "تست شبکه پروفی‌باس و ریداندنسی کنترلرها", 1564, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Server Room", "شبکه Profibus", 2.0, "1404/10/21", "1404/10/21", "pending", 0)
        add(1571, "4.4.2", "تهیه Backup کامل از کل سیستم کنترل DCS و برنامه‌های PLC", 1564, 4, "ابزاردقیق و اتوماسیون", "Core Area", "Server Room", "DCS Backup", 1.0, "1404/10/21", "1404/10/21", "pending", 0)
        add(1578, "4.4.3", "انجام تست‌ها، رفع عیب و سرویس کلی سیستم UPS 20KVA و 30KVA ناحیه Core", 1564, 4, "ابزاردقیق و اتوماسیون", "Core Area", "UPS Room", "UPS Systems", 8.0, "1404/10/14", "1404/10/14", "completed", 100, 3, 8.0)
        add(1591, "4.4.4", "انجام PM و کالیبراسیون ترانسمیترهای فشار و سطح ناحیه WTP", 1564, 4, "ابزاردقیق و اتوماسیون", "WTP", "WTP Substation", "اتوماسیون WTP", 4.0, "1404/10/24", "1404/10/24", "pending", 0)

        // ==========================================
        // 5. ELECTRICAL UNIT (واحد برق)
        // ==========================================
        add(1602, "5", "واحد برق (Electrical Power & Distribution)", 1, 2, "برق", "Core Area", "پست‌های برق و تابلوها", "ترانس‌ها، سوئیچ‌گیرها و موتورها", 150.0, "1404/10/12", "1404/10/26", "in_progress", 32)
        add(1603, "5.1", "Core Area Substation (پست برق ناحیه Core)", 1602, 3, "برق", "Core Area", "پست برق Core", "SWG 33KV & 6.6KV", 138.0, "1404/10/12", "1404/10/26", "in_progress", 35)
        add(1605, "5.1.1", "تامین روشنایی ایمن درون کوره و محوطه‌های تاریک و برچیدن آن", 1603, 4, "برق", "Core Area", "داخل کوره", "روشنایی موقت", 5.0, "1404/10/12", "1404/10/12", "completed", 100, 3, 5.0)
        add(1683, "5.1.2", "انجام تست‌ها و سرویس‌های سوئیچ‌گیر SWG 33KV ناحیه Core", 1603, 4, "برق", "Core Area", "پست برق", "SWG 33KV", 8.0, "1404/10/18", "1404/10/18", "pending", 0)
        add(1684, "5.1.3", "انجام تست‌ها و سرویس‌های سوئیچ‌گیر SWG 6.6-M10 و M20", 1603, 4, "برق", "Core Area", "پست برق", "SWG 6.6KV", 6.0, "1404/10/16", "1404/10/16", "pending", 0)
        add(1694, "5.1.4", "تست و سرویس‌های ترانسفورماتورهای قدرتی ۳۳ به ۶.۶ کیلوولت H10 و H20", 1603, 4, "برق", "Core Area", "پست برق", "Power Transformers", 6.0, "1404/10/13", "1404/10/13", "completed", 100, 4, 6.0)

        // Electrical -> WTP Substation
        add(1776, "5.2", "WTP Electrical Substation (پست برق ناحیه تصفیه آب WTP)", 1602, 3, "برق", "WTP", "پست برق WTP", "تابلوهای MCC و ترانس WTP", 68.0, "1404/10/16", "1404/10/23", "pending", 0)
        add(1705, "5.2.1", "سرویس، آچارکشی و اندازه‌گیری مقاومت عایقی الکتروموتورهای پمپ‌خانه و فن‌های WTP", 1776, 4, "برق", "WTP", "Pump House", "الکتروموتورهای فشار متوسط", 66.0, "1404/10/16", "1404/10/23", "in_progress", 25, 4, 16.5)

        // Electrical -> MHU Substation
        add(1854, "5.3", "MHU Electrical Substation (پست برق ناحیه حمل مواد)", 1602, 3, "برق", "MHU", "پست برق حمل مواد", "MCC Panel و Power Center", 130.0, "1404/10/13", "1404/10/25", "in_progress", 25)

        // ==========================================
        // 6. UTILITIES / FLUIDS UNIT (واحد انرژی و سیالات)
        // ==========================================
        add(1900, "6", "واحد انرژی و سیالات (Utilities & Fluids Maintenance)", 1, 2, "انرژی و سیالات", "WTP", "تاسیسات آب و گاز", "شبکه‌های آب صنعتی، نیتروژن و گاز طبیعی", 140.0, "1404/10/12", "1404/10/26", "in_progress", 40)
        add(1901, "6.1", "تامین، بالانس و توزیع آب صنعتی، هوای فشرده و نیتروژن در خطوط اورهال", 1900, 3, "انرژی و سیالات", "WTP", "شبکه سیالات", "خطوط تغذیه آب و گاز", 140.0, "1404/10/12", "1404/10/26", "in_progress", 50, 4, 70.0)
        add(1902, "6.2", "تست هیدرواستاتیک و بازرسی شیرآلات اطمینان ایستگاه تقلیل فشار گاز طبیعی", 1900, 3, "انرژی و سیالات", "Core Area", "Gas Pressure Reducing Station", "شیرهای اطمینان PSV", 16.0, "1404/10/14", "1404/10/15", "completed", 100, 3, 16.0)

        // ==========================================
        // 7. QUALITY & INSPECTION UNIT (واحد بازرسی فنی)
        // ==========================================
        add(2000, "7", "واحد بازرسی فنی و کنترل کیفیت (Inspection & NDT)", 1, 2, "بازرسی فنی", "Core Area", "کل سایت", "تجهیزات تست غیرمخرب و ضخامت‌سنجی", 160.0, "1404/10/12", "1404/10/28", "in_progress", 45)
        add(2001, "7.1", "تست NDT و ضخامت‌سنجی آلتراسونیک شل و گنبدی کوره احیا و داکت‌های فرآیندی", 2000, 3, "بازرسی فنی", "Core Area", "کوره و داکت‌ها", "تست‌های NDT", 40.0, "1404/10/14", "1404/10/18", "in_progress", 60, 3, 24.0)
        add(2002, "7.2", "بازرسی جوشکاری و گواهینامه‌های کیفی ساخت شارژ هاپر جدید", 2000, 3, "بازرسی فنی", "Core Area", "Furnace Area", "Charge Hopper", 20.0, "1404/10/15", "1404/10/17", "completed", 100, 2, 20.0)

        return list
    }
}
