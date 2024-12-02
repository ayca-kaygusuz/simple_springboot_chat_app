package com.chatapp.chatapp;

import org.springframework.boot.SpringApplication;

import com.chatapp.ChatappApplication;

public class TestChatappApplication {

	public static void main(String[] args) {
		SpringApplication.from(ChatappApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
