/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret:petclinic-default-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}")
	private long jwtExpiration;

	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(Authentication authentication) {
		CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpiration);

		String roles = userPrincipal.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		return Jwts.builder()
			.subject(userPrincipal.getUsername())
			.claim("roles", roles)
			.claim("userId", userPrincipal.getUserId())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	public String generateToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpiration);

		String roles = user.getRoles().stream().map(role -> "ROLE_" + role.name()).collect(Collectors.joining(","));

		return Jwts.builder()
			.subject(user.getUsername())
			.claim("roles", roles)
			.claim("userId", user.getId())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
		return claims.getSubject();
	}

	public String getRolesFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
		return claims.get("roles", String.class);
	}

	public Integer getUserIdFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
		return claims.get("userId", Integer.class);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

}
