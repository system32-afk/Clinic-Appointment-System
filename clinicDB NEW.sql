CREATE DATABASE  IF NOT EXISTS `medical_consultation_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `medical_consultation_system`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: medical_consultation_system
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `AppointmentID` int NOT NULL AUTO_INCREMENT,
  `PatientID` int NOT NULL,
  `DoctorID` int NOT NULL,
  `ReasonForVisit` varchar(255) DEFAULT NULL,
  `Date` date NOT NULL,
  `Time` time NOT NULL,
  `Status` enum('Pending','In-Progress','Completed','Canceled') DEFAULT 'Pending',
  PRIMARY KEY (`AppointmentID`),
  KEY `fk_appointment_patient` (`PatientID`),
  KEY `fk_appointment_doctor` (`DoctorID`),
  CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`DoctorID`) REFERENCES `doctor` (`DoctorID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`PatientID`) REFERENCES `patient` (`PatientID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `diagnosis`
--

DROP TABLE IF EXISTS `diagnosis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diagnosis` (
  `DiagnosisID` int NOT NULL AUTO_INCREMENT,
  `AppointmentID` int NOT NULL,
  `IllnessID` int NOT NULL,
  `DateDiagnosed` date NOT NULL,
  PRIMARY KEY (`DiagnosisID`),
  KEY `fk_diagnosis_appointment` (`AppointmentID`),
  KEY `fk_diagnosis_illness` (`IllnessID`),
  CONSTRAINT `fk_diagnosis_appointment` FOREIGN KEY (`AppointmentID`) REFERENCES `appointment` (`AppointmentID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_diagnosis_illness` FOREIGN KEY (`IllnessID`) REFERENCES `illness` (`IllnessID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor`
--

DROP TABLE IF EXISTS `doctor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor` (
  `DoctorID` int NOT NULL AUTO_INCREMENT,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `Sex` enum('Male','Female','Other') DEFAULT NULL,
  `SpecializationID` int DEFAULT NULL,
  PRIMARY KEY (`DoctorID`),
  KEY `SpecializationID` (`SpecializationID`),
  CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`SpecializationID`) REFERENCES `specialization` (`SpecializationID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `illness`
--

DROP TABLE IF EXISTS `illness`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `illness` (
  `IllnessID` int NOT NULL AUTO_INCREMENT,
  `IllnessName` varchar(100) NOT NULL,
  PRIMARY KEY (`IllnessID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicalhistory`
--

DROP TABLE IF EXISTS `medicalhistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicalhistory` (
  `MedicalHistoryID` int NOT NULL AUTO_INCREMENT,
  `PatientID` int DEFAULT NULL,
  `BloodType` varchar(100) DEFAULT NULL,
  `Allergies` text,
  `Height` decimal(10,0) DEFAULT NULL,
  `Conditions` varchar(100) DEFAULT NULL,
  `Past_Surgeries` text,
  PRIMARY KEY (`MedicalHistoryID`),
  KEY `PatientID` (`PatientID`),
  CONSTRAINT `medicalhistory_ibfk_1` FOREIGN KEY (`PatientID`) REFERENCES `patient` (`PatientID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicine`
--

DROP TABLE IF EXISTS `medicine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicine` (
  `MedicineID` int NOT NULL AUTO_INCREMENT,
  `MedicineName` varchar(100) NOT NULL,
  PRIMARY KEY (`MedicineID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient` (
  `PatientID` int NOT NULL AUTO_INCREMENT,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `Age` int DEFAULT NULL,
  `Sex` enum('Male','Female','Other') DEFAULT NULL,
  `ContactNumber` varchar(20) DEFAULT NULL,
  `BuildingNo` varchar(45) DEFAULT NULL,
  `Street` varchar(100) DEFAULT NULL,
  `BarangayNo` varchar(45) DEFAULT NULL,
  `City` varchar(100) DEFAULT NULL,
  `Province` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`PatientID`),
  UNIQUE KEY `unique_patient` (`FirstName`,`LastName`,`ContactNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `PaymentID` int NOT NULL AUTO_INCREMENT,
  `ProcedureID` int NOT NULL,
  `PaymentDate` date NOT NULL,
  `AmountDue` decimal(10,2) NOT NULL,
  `ModeOfPayment` enum('Cash','Credit Card','Debit Card','Insurance','Other') NOT NULL,
  PRIMARY KEY (`PaymentID`),
  KEY `fk_payment_procedure` (`ProcedureID`),
  CONSTRAINT `fk_payment_procedure` FOREIGN KEY (`ProcedureID`) REFERENCES `procedurerequest` (`ProcedureID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prescription`
--

DROP TABLE IF EXISTS `prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription` (
  `PrescriptionID` int NOT NULL AUTO_INCREMENT,
  `AppointmentID` int NOT NULL,
  `IllnessID` int NOT NULL,
  `MedicineID` int NOT NULL,
  `Dosage` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`PrescriptionID`),
  KEY `fk_prescription_appointment` (`AppointmentID`),
  KEY `fk_prescription_illness` (`IllnessID`),
  KEY `fk_prescription_medicine` (`MedicineID`),
  CONSTRAINT `fk_prescription_appointment` FOREIGN KEY (`AppointmentID`) REFERENCES `appointment` (`AppointmentID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_illness` FOREIGN KEY (`IllnessID`) REFERENCES `illness` (`IllnessID`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_medicine` FOREIGN KEY (`MedicineID`) REFERENCES `medicine` (`MedicineID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `procedurerequest`
--

DROP TABLE IF EXISTS `procedurerequest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `procedurerequest` (
  `ProcedureID` int NOT NULL AUTO_INCREMENT,
  `AppointmentID` int NOT NULL,
  `ServiceID` int NOT NULL,
  `ProcedureDate` date NOT NULL,
  PRIMARY KEY (`ProcedureID`),
  KEY `fk_procedure_appointment` (`AppointmentID`),
  KEY `fk_procedure_service` (`ServiceID`),
  CONSTRAINT `fk_procedure_appointment` FOREIGN KEY (`AppointmentID`) REFERENCES `appointment` (`AppointmentID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_procedure_service` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ServiceID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `ServiceID` int NOT NULL AUTO_INCREMENT,
  `ServiceName` varchar(100) NOT NULL,
  `Price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`ServiceID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `specialization`
--

DROP TABLE IF EXISTS `specialization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `specialization` (
  `SpecializationID` int NOT NULL AUTO_INCREMENT,
  `SpecializationName` varchar(100) NOT NULL,
  PRIMARY KEY (`SpecializationID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-12  0:40:11
