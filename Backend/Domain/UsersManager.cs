﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using Backend.Data.Database;
using Backend.Data.Entities;
using Backend.Data.Repositories;

namespace Backend.Domain
{
    // ReSharper disable once ClassNeverInstantiated.Global
    public class UsersManager
    {
        private readonly UserRepository _userRepository;
        private readonly DatabaseContext _databaseContext;
        private readonly SessionRepository _sessionRepository;

        public UsersManager(UserRepository userRepository, DatabaseContext databaseContext, SessionRepository sessionRepository)
        {
            _userRepository = userRepository;
            _databaseContext = databaseContext;
            _sessionRepository = sessionRepository;
        }

        public IEnumerable<User> GetUsers(bool readPrivateFields)
        {
            return _userRepository.GetUsers(readPrivateFields);
        }
        
        public User GetUser(int id, bool readPrivateFields)
        {
            return _userRepository.GetUser(id, readPrivateFields);
        }
        
        public Session Register(CreateUserRequest createUserRequest)
        {
            return _databaseContext.UseTransaction(transaction =>
            {
                _userRepository.AddUser(createUserRequest.User, transaction);
                _userRepository.AddLogin(createUserRequest.User, Hash(createUserRequest.Password), transaction);
                
                var session = CreateSession(createUserRequest.User);
                
                _sessionRepository.AddSession(session, transaction);
                
                return session;
            });
        }
        
        public Session Login(CreateSessionRequest createSessionRequest)
        {
            return _databaseContext.UseTransaction(transaction =>
            {
                var user = _userRepository.GetUserByEmail(createSessionRequest.Email, true, transaction);
                if (user == null)
                    throw new Exception("User does not exist");

                Debug.Assert(user.Id != null, "user.Id != null");
                var login = _userRepository.GetLogin(user.Id.Value, transaction);
                if (login == null)
                    throw new Exception("Login does not exist");

                var hash = Hash(createSessionRequest.Password);

                if (!login.Hash.SequenceEqual(hash))
                    throw new Exception("Password is not correct");

                var session = CreateSession(user);

                _sessionRepository.AddSession(session, transaction);
                
                return session;
            });
        }
        
        public Session FindSession(string token)
        {
            return _sessionRepository.GetSessionByToken(token);
        }

        public User UpdateUser(User user)
        {
            Debug.Assert(user.Id != null, "user.Id != null");
            
            return _databaseContext.UseTransaction(transaction =>
            {
                _userRepository.UpdateUser(user, transaction);
                return _userRepository.GetUser(user.Id.Value, true, transaction);
            });
        }
        
        private static byte[] Hash(string input)
        {
            return Hash(Encoding.UTF8.GetBytes(input));
        }

        private static byte[] Hash(byte[] input)
        {
            using (var sha1 = new SHA1Managed())
            {
                return sha1.ComputeHash(input);
            }
        }

        private static string ConvertBytesToString(IEnumerable<byte> input)
        {
            var sb = new StringBuilder();
            foreach (var b in input)
            {
                sb.Append(b.ToString("X2"));
            }

            return sb.ToString();
        }

        private static Session CreateSession(User user)
        {
            var random = new Random();
            
            var bytes = new byte[40];
            random.NextBytes(bytes);

            var hash = Hash(bytes);

            return new Session
            {
                Token = ConvertBytesToString(hash),
                User = user
            };
        }

        public void UpdatePassword(int userId, string password)
        {
            _userRepository.UpdatePassword(userId, Hash(password));
        }
    }
}