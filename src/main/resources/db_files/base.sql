-- MySQL Script generated by MySQL Workbench -- Mon Apr 29 16:24:30 2019 -- Model: New Model Version: 1.0 -- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'TRADITIONAL,ALLOW_INVALID_DATES'; -- ----------------------------------------------------- -- Schema mydb -- ----------------------------------------------------- -- ----------------------------------------------------- -- Schema mydb -- -----------------------------------------------------

CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER

    SET utf8;

USE `mydb`;

-- ----------------------------------------------------- -- TABLE `mydb`.`user` -- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `mydb`.`user` ( `id` INT NOT NULL, `username` VARCHAR(45) NOT NULL, `email` VARCHAR(45) NOT NULL, `pseudo` VARCHAR(255) NULL, `password` VARCHAR(255) NOT NULL, `created_at` DATETIME NOT NULL, PRIMARY KEY (`id`), UNIQUE INDEX `email_UNIQUE` (`email` ASC), UNIQUE INDEX `username_UNIQUE` (`username` ASC)) ENGINE = InnoDB; -- ----------------------------------------------------- -- TABLE `mydb`.`video` -- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `mydb`.`video` ( `id` INT NOT NULL, `name` VARCHAR(45) NOT NULL, `duration` INT NULL, `user_id` INT NOT NULL, `source` VARCHAR(45) NOT NULL, `created_at` DATETIME NOT NULL, `view` INT NOT NULL, `enabled` TINYINT(1) NOT NULL, PRIMARY KEY (`id`), INDEX `fk_video_user_idx` (`user_id` ASC), CONSTRAINT `fk_video_user` FOREIGN KEY (`user_id`) REFERENCES `mydb`.`user` (`id`)
    ON
        DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB; -- ----------------------------------------------------- -- TABLE `mydb`.`video_format` -- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `mydb`.`video_format` ( `id` INT NOT NULL, `code` VARCHAR(45) NOT NULL, `uri` VARCHAR(45) NOT NULL, `video_id` INT NOT NULL, PRIMARY KEY (`id`), INDEX `fk_video_format_video1_idx` (`video_id` ASC), CONSTRAINT `fk_video_format_video1` FOREIGN KEY (`video_id`) REFERENCES `mydb`.`video` (`id`)
    ON
        DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB; -- ----------------------------------------------------- -- TABLE `mydb`.`token` -- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `mydb`.`token` ( `id` INT NOT NULL, `code` VARCHAR(45) NOT NULL, `expired_at` DATETIME NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`id`), INDEX `fk_token_user1_idx` (`user_id` ASC), UNIQUE INDEX `code_UNIQUE` (`code` ASC), CONSTRAINT `fk_token_user1` FOREIGN KEY (`user_id`) REFERENCES `mydb`.`user` (`id`)
    ON
        DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB; -- ----------------------------------------------------- -- TABLE `mydb`.`comment` -- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `mydb`.`comment` ( `id` INT NOT NULL, `body` LONGTEXT NULL, `user_id` INT NOT NULL, `video_id` INT NOT NULL, PRIMARY KEY (`id`), INDEX `fk_comment_user1_idx` (`user_id` ASC), INDEX `fk_comment_video1_idx` (`video_id` ASC), CONSTRAINT `fk_comment_user1` FOREIGN KEY (`user_id`) REFERENCES `mydb`.`user` (`id`)
    ON
        DELETE NO ACTION
    ON UPDATE NO ACTION, CONSTRAINT `fk_comment_video1` FOREIGN KEY (`video_id`) REFERENCES `mydb`.`video` (`id`)
    ON
        DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB;

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;