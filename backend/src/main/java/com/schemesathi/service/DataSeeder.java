package com.schemesathi.service;

import com.schemesathi.entity.*;
import com.schemesathi.repository.CitizenProfileRepository;
import com.schemesathi.repository.SchemeRepository;
import com.schemesathi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with verified government scheme data on first startup.
 * All scheme information is sourced from official government portals.
 */
@Component
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SchemeRepository schemeRepository;
    private final UserRepository userRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SchemeRepository schemeRepository,
                      UserRepository userRepository,
                      CitizenProfileRepository citizenProfileRepository,
                      PasswordEncoder passwordEncoder) {
        this.schemeRepository = schemeRepository;
        this.userRepository = userRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedDefaultUsers();

        if (schemeRepository.count() > 0) {
            log.info("Database already seeded with schemes. Skipping scheme data.");
            return;
        }

        log.info("Seeding government scheme data...");
        seedSchemes();
        log.info("Data seeding complete. {} schemes loaded.", schemeRepository.count());
    }

    private void seedDefaultUsers() {
        if (!userRepository.existsByEmail("admin@schemesathi.in")) {
            User admin = User.builder()
                    .email("admin@schemesathi.in")
                    .password(passwordEncoder.encode("Admin@SchemeSathi2024"))
                    .fullName("System Admin")
                    .role(User.UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created.");
        }

        if (!userRepository.existsByEmail("citizen@schemesathi.in")) {
            User citizen = User.builder()
                    .email("citizen@schemesathi.in")
                    .password(passwordEncoder.encode("Citizen@123"))
                    .fullName("Ramesh Kumar (Demo Citizen)")
                    .phone("9876543210")
                    .role(User.UserRole.CITIZEN)
                    .build();
            citizen = userRepository.save(citizen);
            log.info("Demo Citizen user created.");

            if (citizenProfileRepository.findByUserId(citizen.getId()).isEmpty()) {
                CitizenProfile profile = CitizenProfile.builder()
                        .user(citizen)
                        .age(42)
                        .gender("Male")
                        .state("Telangana")
                        .district("Hyderabad")
                        .occupation("Farmer / Agriculture")
                        .annualIncome(95000.0)
                        .educationLevel("10th")
                        .category("OBC")
                        .landOwnership(true)
                        .landArea(2.5)
                        .employmentStatus("SelfEmployed")
                        .residenceType("Rural")
                        .maritalStatus("Married")
                        .familySize(4)
                        .bplStatus(true)
                        .rationCardType("PHH")
                        .build();
                citizenProfileRepository.save(profile);
                log.info("Demo Citizen profile created.");
            }
        }
    }

    private void seedSchemes() {
        // 1. PM Kisan Samman Nidhi
        Scheme pmKisan = Scheme.builder()
                .name("PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)")
                .description("Direct income support of ₹6,000 per year to all landholding farmer families in three equal installments of ₹2,000 each, every four months.")
                .objective("To supplement the financial needs of land-holding farmers in procuring various inputs to ensure proper crop health and appropriate yields.")
                .governmentAuthority("Ministry of Agriculture & Farmers Welfare")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Agriculture")
                .benefits("₹6,000 per year transferred directly to bank account in three installments of ₹2,000 each.")
                .applicationProcess("1. Visit pmkisan.gov.in\n2. Click on 'New Farmer Registration'\n3. Enter Aadhaar number and captcha\n4. Fill in personal and bank details\n5. Submit land ownership documents\n6. Application reviewed by State Nodal Officer")
                .officialUrl("https://pmkisan.gov.in/")
                .applicationUrl("https://pmkisan.gov.in/RegistrationForm.aspx")
                .source("Ministry of Agriculture & Farmers Welfare, Government of India")
                .verificationDate(LocalDate.of(2024, 6, 15))
                .importantWarnings("Income tax payers are NOT eligible. Institutional land holders are excluded. All family members' Aadhaar must be linked.")
                .commonRejectionReasons("1. Aadhaar not linked to bank account\n2. Wrong bank details\n3. Income tax payer\n4. Institutional land holder\n5. Family member is government employee")
                .active(true)
                .build();

        pmKisan.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(pmKisan).criterionField("landOwnership").operator("BOOL").value("true").displayLabel("Land Ownership").description("Must own agricultural land").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(pmKisan).criterionField("occupation").operator("IN").value("Farmer,Agriculture,Agricultural Labourer").displayLabel("Occupation").description("Must be a farmer or involved in agriculture").mandatory(true).priority(2).build()
        ));

        pmKisan.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(pmKisan).documentName("Aadhaar Card").description("Aadhaar number must be linked to bank account").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmKisan).documentName("Land Ownership Records").description("Khasra/Khatauni or equivalent land documents").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmKisan).documentName("Bank Account Details").description("Active bank account with IFSC code").mandatory(true).build()
        ));
        schemeRepository.save(pmKisan);

        // 2. PM Awas Yojana (PMAY)
        Scheme pmay = Scheme.builder()
                .name("Pradhan Mantri Awas Yojana (PMAY)")
                .description("Financial assistance for construction/purchase of a pucca house for eligible families who do not own a pucca house. Covers both urban and rural areas.")
                .objective("Housing for All by providing affordable housing to the urban and rural poor.")
                .governmentAuthority("Ministry of Housing and Urban Affairs / Ministry of Rural Development")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Housing")
                .benefits("Subsidy up to ₹2.67 lakh for EWS/LIG categories under CLSS. Rural: ₹1.2 lakh for plain areas, ₹1.3 lakh for hilly/difficult areas.")
                .applicationProcess("1. Visit pmaymis.gov.in or apply through CSC center\n2. Select 'Citizen Assessment'\n3. Enter Aadhaar number\n4. Fill personal, income, and family details\n5. Submit application with required documents\n6. Application verified by Urban Local Body")
                .officialUrl("https://pmaymis.gov.in/")
                .applicationUrl("https://pmaymis.gov.in/")
                .source("Ministry of Housing and Urban Affairs, Government of India")
                .verificationDate(LocalDate.of(2024, 5, 20))
                .importantWarnings("Beneficiary family should not own a pucca house in any part of India. Only one benefit per family.")
                .commonRejectionReasons("1. Already owns a pucca house\n2. Already availed housing benefit from government\n3. Income above limit\n4. Incomplete documentation")
                .active(true)
                .build();

        pmay.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(pmay).criterionField("annualIncome").operator("LTE").value("300000").displayLabel("Annual Income").description("Annual household income should be ≤ ₹3 lakh for EWS category").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(pmay).criterionField("category").operator("IN").value("EWS,LIG,SC,ST,OBC,General").displayLabel("Category").description("All categories eligible with income criteria").mandatory(false).priority(3).build()
        ));

        pmay.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(pmay).documentName("Aadhaar Card").description("Aadhaar card of all family members").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmay).documentName("Income Certificate").description("Annual income certificate from competent authority").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmay).documentName("Bank Account Details").description("Active savings bank account").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmay).documentName("Affidavit").description("Affidavit declaring no pucca house ownership").mandatory(true).build()
        ));
        schemeRepository.save(pmay);

        // 3. National Scholarship Portal Schemes
        Scheme nsp = Scheme.builder()
                .name("Post-Matric Scholarship for SC Students")
                .description("Scholarship for students belonging to Scheduled Castes studying at post-matriculation or post-secondary stage to enable them to complete their education.")
                .objective("Financial support to SC students for higher education.")
                .governmentAuthority("Ministry of Social Justice and Empowerment")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Education")
                .benefits("Full tuition fee reimbursement + maintenance allowance ranging from ₹380 to ₹1,200 per month depending on course and hosteller/day-scholar status.")
                .applicationProcess("1. Visit scholarships.gov.in\n2. Register using valid mobile number and email\n3. Login and select 'Post-Matric Scholarship for SC'\n4. Fill academic and personal details\n5. Upload required documents\n6. Submit for verification by institute and district authority")
                .officialUrl("https://scholarships.gov.in/")
                .applicationUrl("https://scholarships.gov.in/")
                .source("Ministry of Social Justice and Empowerment, Government of India")
                .verificationDate(LocalDate.of(2024, 7, 10))
                .importantWarnings("Application window is usually July to November. Renewal applications must be submitted annually. Attendance requirements apply.")
                .commonRejectionReasons("1. Income above ₹2.5 lakh limit\n2. Not enrolled in recognized institution\n3. Failed to submit renewal on time\n4. Invalid caste certificate")
                .active(true)
                .build();

        nsp.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(nsp).criterionField("category").operator("EQ").value("SC").displayLabel("Category").description("Must belong to Scheduled Caste").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(nsp).criterionField("annualIncome").operator("LTE").value("250000").displayLabel("Family Income").description("Annual family income should be ≤ ₹2.5 lakh").mandatory(true).priority(2).build(),
                EligibilityRule.builder().scheme(nsp).criterionField("educationLevel").operator("IN").value("12th,Graduate,PostGraduate,PhD").displayLabel("Education Level").description("Must be studying at post-matriculation level").mandatory(true).priority(3).build()
        ));

        nsp.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(nsp).documentName("Caste Certificate").description("SC caste certificate issued by competent authority").mandatory(true).build(),
                RequiredDocument.builder().scheme(nsp).documentName("Income Certificate").description("Family income certificate (annual income ≤ ₹2.5 lakh)").mandatory(true).build(),
                RequiredDocument.builder().scheme(nsp).documentName("Previous Year Marksheet").description("Marks/grade sheet of last qualifying exam").mandatory(true).build(),
                RequiredDocument.builder().scheme(nsp).documentName("Aadhaar Card").description("Aadhaar card of the student").mandatory(true).build(),
                RequiredDocument.builder().scheme(nsp).documentName("Bank Account Details").description("Bank passbook with Aadhaar-linked account").mandatory(true).build()
        ));
        schemeRepository.save(nsp);

        // 4. Ayushman Bharat (PMJAY)
        Scheme pmjay = Scheme.builder()
                .name("Ayushman Bharat - Pradhan Mantri Jan Arogya Yojana (PMJAY)")
                .description("World's largest health insurance scheme providing health cover of ₹5 lakh per family per year for secondary and tertiary care hospitalization.")
                .objective("To reduce catastrophic out-of-pocket health expenditure and improve access to quality healthcare.")
                .governmentAuthority("National Health Authority")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Health")
                .benefits("Health cover of ₹5 lakh per family per year. Cashless and paperless access at empanelled hospitals. Covers pre and post hospitalization expenses.")
                .applicationProcess("1. Visit mera.pmjay.gov.in to check eligibility\n2. Enter mobile number or ration card number\n3. If eligible, visit nearest CSC center or empanelled hospital\n4. Complete e-KYC with Aadhaar\n5. Get Ayushman Card generated")
                .officialUrl("https://pmjay.gov.in/")
                .applicationUrl("https://mera.pmjay.gov.in/")
                .source("National Health Authority, Government of India")
                .verificationDate(LocalDate.of(2024, 6, 1))
                .importantWarnings("Eligibility is based on SECC 2011 data. Not all hospitals are empanelled. Pre-existing diseases are covered from day one.")
                .commonRejectionReasons("1. Family not found in SECC database\n2. Aadhaar not linked\n3. Hospital not empanelled\n4. Treatment not covered under package list")
                .active(true)
                .build();

        pmjay.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(pmjay).criterionField("annualIncome").operator("LTE").value("500000").displayLabel("Family Income").description("Based on deprivation criteria from SECC data").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(pmjay).criterionField("bplStatus").operator("BOOL").value("true").displayLabel("BPL Status").description("Below Poverty Line status or deprivation criteria").mandatory(false).priority(2).build()
        ));

        pmjay.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(pmjay).documentName("Aadhaar Card").description("For e-KYC verification").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmjay).documentName("Ration Card").description("Family ration card for verification").mandatory(false).alternatives("SECC Reference Number").build(),
                RequiredDocument.builder().scheme(pmjay).documentName("Mobile Number").description("Active mobile number linked to Aadhaar").mandatory(true).build()
        ));
        schemeRepository.save(pmjay);

        // 5. Sukanya Samriddhi Yojana
        Scheme ssy = Scheme.builder()
                .name("Sukanya Samriddhi Yojana")
                .description("A small savings scheme for the girl child under 'Beti Bachao, Beti Padhao' campaign. Offers attractive interest rate and tax benefits.")
                .objective("To build a fund for the future education and marriage expenses of a girl child.")
                .governmentAuthority("Ministry of Finance, Department of Economic Affairs")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Women Empowerment")
                .benefits("Current interest rate: 8.2% per annum (Q1 2024-25). Tax benefits under Section 80C. Minimum deposit: ₹250/year. Maximum: ₹1.5 lakh/year. Account matures when girl turns 21.")
                .applicationProcess("1. Visit nearest post office or authorized bank\n2. Fill account opening form\n3. Submit birth certificate of girl child\n4. Submit ID and address proof of guardian\n5. Make initial deposit (minimum ₹250)\n6. Receive passbook")
                .officialUrl("https://www.india.gov.in/sukanya-samriddhi-yojana")
                .applicationUrl("https://www.indiapost.gov.in/")
                .source("Department of Economic Affairs, Ministry of Finance")
                .verificationDate(LocalDate.of(2024, 4, 1))
                .importantWarnings("Only for girl child below 10 years of age at the time of account opening. Maximum two accounts per family (one per girl child).")
                .commonRejectionReasons("1. Girl child above 10 years\n2. More than 2 accounts in family\n3. Incomplete guardian documentation")
                .active(true)
                .build();

        ssy.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(ssy).criterionField("gender").operator("EQ").value("Female").displayLabel("Gender").description("Account is only for girl child").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(ssy).criterionField("age").operator("LTE").value("10").displayLabel("Age").description("Girl child must be below 10 years at the time of account opening").mandatory(true).priority(2).build()
        ));

        ssy.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(ssy).documentName("Birth Certificate").description("Birth certificate of the girl child").mandatory(true).build(),
                RequiredDocument.builder().scheme(ssy).documentName("Guardian ID Proof").description("Aadhaar/PAN/Passport of parent/guardian").mandatory(true).build(),
                RequiredDocument.builder().scheme(ssy).documentName("Address Proof").description("Address proof of parent/guardian").mandatory(true).build()
        ));
        schemeRepository.save(ssy);

        // 6. PM Ujjwala Yojana
        Scheme pmuy = Scheme.builder()
                .name("Pradhan Mantri Ujjwala Yojana (PMUY)")
                .description("Provides free LPG connections to women from Below Poverty Line (BPL) households, improving health outcomes and reducing indoor air pollution.")
                .objective("To provide clean cooking fuel to BPL households and reduce health hazards associated with burning traditional fuels.")
                .governmentAuthority("Ministry of Petroleum and Natural Gas")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Social Security")
                .benefits("Free LPG connection. Financial assistance of ₹1,600 for each connection. Option for EMI for first refill and hot plate.")
                .applicationProcess("1. Visit nearest LPG distributor (Indian Oil, Bharat Petroleum, HP Gas)\n2. Fill PMUY application form (KYC-1)\n3. Submit required documents\n4. Verification by distributor\n5. Receive LPG connection with deposit-free cylinder")
                .officialUrl("https://www.pmujjwalayojana.com/")
                .applicationUrl("https://www.pmujjwalayojana.com/")
                .source("Ministry of Petroleum and Natural Gas, Government of India")
                .verificationDate(LocalDate.of(2024, 3, 15))
                .importantWarnings("Only one connection per household. Must not have an existing LPG connection in any family member's name.")
                .commonRejectionReasons("1. Already has LPG connection\n2. Not in SECC/BPL list\n3. Duplicate application\n4. Invalid address proof")
                .active(true)
                .build();

        pmuy.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(pmuy).criterionField("gender").operator("EQ").value("Female").displayLabel("Gender").description("Connection is given in the name of adult woman of the household").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(pmuy).criterionField("age").operator("GTE").value("18").displayLabel("Age").description("Applicant must be 18 years or above").mandatory(true).priority(2).build(),
                EligibilityRule.builder().scheme(pmuy).criterionField("bplStatus").operator("BOOL").value("true").displayLabel("BPL Status").description("Must belong to BPL household").mandatory(true).priority(3).build()
        ));

        pmuy.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(pmuy).documentName("BPL Ration Card").description("BPL ration card or SECC reference").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmuy).documentName("Aadhaar Card").description("Aadhaar card of the applicant").mandatory(true).build(),
                RequiredDocument.builder().scheme(pmuy).documentName("Bank Account Details").description("Jan Dhan or savings bank account").mandatory(true).build()
        ));
        schemeRepository.save(pmuy);

        // 7. MGNREGA
        Scheme mgnrega = Scheme.builder()
                .name("Mahatma Gandhi National Rural Employment Guarantee Act (MGNREGA)")
                .description("Guarantees 100 days of wage employment in a financial year to every rural household whose adult members volunteer to do unskilled manual work.")
                .objective("To enhance livelihood security of rural households by guaranteeing 100 days of wage employment.")
                .governmentAuthority("Ministry of Rural Development")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Employment")
                .benefits("100 days of guaranteed wage employment per household per year. Minimum wages as per state notification. Unemployment allowance if work not provided within 15 days.")
                .applicationProcess("1. Apply at Gram Panchayat office\n2. Submit application for registration\n3. Receive Job Card (within 15 days)\n4. Apply for work using Job Card\n5. Work must be provided within 15 days\n6. Wages credited to bank account")
                .officialUrl("https://nrega.nic.in/")
                .applicationUrl("https://nrega.nic.in/")
                .source("Ministry of Rural Development, Government of India")
                .verificationDate(LocalDate.of(2024, 5, 1))
                .importantWarnings("Work must be demanded in writing. If work not provided within 15 days, unemployment allowance must be paid. Work should be within 5 km of habitation.")
                .commonRejectionReasons("1. Urban area resident\n2. Not willing to do manual work\n3. Job card discrepancies")
                .active(true)
                .build();

        mgnrega.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(mgnrega).criterionField("age").operator("GTE").value("18").displayLabel("Age").description("Must be an adult member of household").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(mgnrega).criterionField("residenceType").operator("EQ").value("Rural").displayLabel("Residence Type").description("Must be a rural household").mandatory(true).priority(2).build()
        ));

        mgnrega.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(mgnrega).documentName("Aadhaar Card").description("Aadhaar card for identity verification").mandatory(true).build(),
                RequiredDocument.builder().scheme(mgnrega).documentName("Address Proof").description("Proof of rural residence").mandatory(true).build(),
                RequiredDocument.builder().scheme(mgnrega).documentName("Bank Account Details").description("Bank account for wage transfer").mandatory(true).build(),
                RequiredDocument.builder().scheme(mgnrega).documentName("Photograph").description("Passport size photograph").mandatory(true).build()
        ));
        schemeRepository.save(mgnrega);

        // 8. Telangana Kalyana Lakshmi / Shaadi Mubarak
        Scheme tsKalyana = Scheme.builder()
                .name("Telangana Kalyana Lakshmi / Shaadi Mubarak")
                .description("Financial assistance for marriage of daughters from economically weaker families in Telangana. ₹1,00,116 for Hindu families (Kalyana Lakshmi) and Muslim families (Shaadi Mubarak).")
                .objective("To provide financial assistance for the marriage of girls from economically weaker sections in Telangana.")
                .governmentAuthority("Government of Telangana, Women Development & Child Welfare Department")
                .scope(Scheme.SchemeScope.STATE)
                .state("Telangana")
                .benefitCategory("Women Empowerment")
                .benefits("₹1,00,116 financial assistance for the marriage of the bride.")
                .applicationProcess("1. Apply through MeeSeva portal or MeeSeva center\n2. Submit application with required documents\n3. Verification by Mandal Revenue Officer\n4. Amount transferred to bride's bank account after verification")
                .officialUrl("https://telanganaepass.cgg.gov.in/")
                .applicationUrl("https://telanganaepass.cgg.gov.in/")
                .source("Government of Telangana")
                .verificationDate(LocalDate.of(2024, 4, 15))
                .importantWarnings("Bride must be 18+ years. Application must be submitted before marriage. Family income limit applies.")
                .commonRejectionReasons("1. Income above limit\n2. Bride below 18 years\n3. Application submitted after marriage\n4. Incorrect documentation")
                .active(true)
                .build();

        tsKalyana.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(tsKalyana).criterionField("state").operator("EQ").value("Telangana").displayLabel("State").description("Must be a resident of Telangana").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(tsKalyana).criterionField("gender").operator("EQ").value("Female").displayLabel("Gender").description("Benefit is for the bride").mandatory(true).priority(2).build(),
                EligibilityRule.builder().scheme(tsKalyana).criterionField("age").operator("GTE").value("18").displayLabel("Age").description("Bride must be 18 years or above").mandatory(true).priority(3).build(),
                EligibilityRule.builder().scheme(tsKalyana).criterionField("annualIncome").operator("LTE").value("200000").displayLabel("Family Income").description("Annual family income should not exceed ₹2 lakh").mandatory(true).priority(4).build()
        ));

        tsKalyana.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(tsKalyana).documentName("Aadhaar Card").description("Aadhaar card of the bride").mandatory(true).build(),
                RequiredDocument.builder().scheme(tsKalyana).documentName("Income Certificate").description("Income certificate from MRO").mandatory(true).build(),
                RequiredDocument.builder().scheme(tsKalyana).documentName("Age Proof").description("Birth certificate or SSC certificate for age verification").mandatory(true).build(),
                RequiredDocument.builder().scheme(tsKalyana).documentName("Caste Certificate").description("Caste certificate if applicable").mandatory(false).build(),
                RequiredDocument.builder().scheme(tsKalyana).documentName("Bank Passbook").description("Bank account details of the bride").mandatory(true).build()
        ));
        schemeRepository.save(tsKalyana);

        // 9. National Pension System (NPS)
        Scheme nps = Scheme.builder()
                .name("Atal Pension Yojana (APY)")
                .description("Guaranteed minimum pension scheme for workers in the unorganised sector. Provides fixed pension of ₹1,000 to ₹5,000 per month after age 60.")
                .objective("To create a universal social security system for all Indians, especially the underprivileged and workers in the unorganised sector.")
                .governmentAuthority("Pension Fund Regulatory and Development Authority (PFRDA)")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Social Security")
                .benefits("Guaranteed pension of ₹1,000 to ₹5,000 per month after age 60. Government co-contribution of 50% for 5 years (for non-tax payers). Spouse continues to receive pension after subscriber's death.")
                .applicationProcess("1. Visit bank branch where savings account is maintained\n2. Fill APY registration form\n3. Provide Aadhaar and mobile number\n4. Choose pension amount (₹1,000 to ₹5,000)\n5. Auto-debit mandate set up from savings account\n6. PRAN (Permanent Retirement Account Number) generated")
                .officialUrl("https://www.npscra.nsdl.co.in/scheme-details.php")
                .applicationUrl("https://enps.nsdl.com/eNPS/NationalPensionSystem.html")
                .source("PFRDA, Government of India")
                .verificationDate(LocalDate.of(2024, 5, 10))
                .importantWarnings("Must join before age 40. Monthly contributions vary based on age and desired pension. Early exit not allowed except in case of terminal illness.")
                .commonRejectionReasons("1. Age above 40 years\n2. Already enrolled in NPS\n3. Income tax payer (for government co-contribution)\n4. Invalid bank account")
                .active(true)
                .build();

        nps.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(nps).criterionField("age").operator("BETWEEN").value("18:40").displayLabel("Age").description("Must be between 18 and 40 years").mandatory(true).priority(1).build()
        ));

        nps.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(nps).documentName("Aadhaar Card").description("Aadhaar number for KYC").mandatory(true).build(),
                RequiredDocument.builder().scheme(nps).documentName("Bank Account Details").description("Active savings bank account for auto-debit").mandatory(true).build(),
                RequiredDocument.builder().scheme(nps).documentName("Mobile Number").description("Active mobile number").mandatory(true).build()
        ));
        schemeRepository.save(nps);

        // 10. PM Mudra Yojana
        Scheme mudra = Scheme.builder()
                .name("Pradhan Mantri MUDRA Yojana (PMMY)")
                .description("Provides loans up to ₹10 lakh to non-corporate, non-farm small/micro enterprises. Three categories: Shishu (up to ₹50,000), Kishor (₹50,001 to ₹5 lakh), Tarun (₹5,00,001 to ₹10 lakh).")
                .objective("To fund the unfunded micro enterprises and support entrepreneurs in starting or expanding their businesses.")
                .governmentAuthority("Ministry of Finance")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Employment")
                .benefits("Collateral-free loans. Shishu: up to ₹50,000. Kishor: ₹50,001 to ₹5,00,000. Tarun: ₹5,00,001 to ₹10,00,000. No processing fee. Flexible repayment period up to 5 years.")
                .applicationProcess("1. Visit nearest bank, NBFC, or MFI\n2. Fill MUDRA loan application form\n3. Submit business plan/project report\n4. Submit required documents\n5. Bank processes application\n6. Loan disbursed to bank account")
                .officialUrl("https://www.mudra.org.in/")
                .applicationUrl("https://www.mudra.org.in/")
                .source("MUDRA (Micro Units Development & Refinance Agency), Ministry of Finance")
                .verificationDate(LocalDate.of(2024, 4, 20))
                .importantWarnings("Not for existing defaulters. Business should be non-farm sector. Manufacturing, trading, services sector eligible.")
                .commonRejectionReasons("1. Poor credit history\n2. Existing loan default\n3. Incomplete business plan\n4. Ineligible business category")
                .active(true)
                .build();

        mudra.getEligibilityRules().addAll(List.of(
                EligibilityRule.builder().scheme(mudra).criterionField("age").operator("GTE").value("18").displayLabel("Age").description("Must be 18 years or above").mandatory(true).priority(1).build(),
                EligibilityRule.builder().scheme(mudra).criterionField("employmentStatus").operator("IN").value("SelfEmployed,Unemployed").displayLabel("Employment").description("For self-employed or aspiring entrepreneurs").mandatory(true).priority(2).build()
        ));

        mudra.getRequiredDocuments().addAll(List.of(
                RequiredDocument.builder().scheme(mudra).documentName("Identity Proof").description("Aadhaar/PAN/Voter ID/Passport").mandatory(true).build(),
                RequiredDocument.builder().scheme(mudra).documentName("Address Proof").description("Aadhaar/Utility bill/Rent agreement").mandatory(true).build(),
                RequiredDocument.builder().scheme(mudra).documentName("Business Plan").description("Brief project report").mandatory(true).build(),
                RequiredDocument.builder().scheme(mudra).documentName("Passport Size Photos").description("2 recent photographs").mandatory(true).build()
        ));
        schemeRepository.save(mudra);
    }
}
