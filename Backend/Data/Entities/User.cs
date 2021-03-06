﻿using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;

namespace Backend.Data.Entities
{
    public class User
    {
        public const string RoleAdmin = "Admin";
        
        public int? Id { get; set; }
        [Required]
        public string FirstName { get; set; }
        [Required]
        public string LastName { get; set; }
        public string City { get; set; }
        // ReSharper disable once InconsistentNaming
        public string WCAID { get; set; }
        public string PhoneNumber { get; set; }
        [Required]
        public Gender? Gender { get; set; }
        public DateTime? BirthDate { get; set; }
        public DateTime? Approved { get; set; }
        [Required]
        public string Email { get; set; }
        public IEnumerable<string> Roles { get; set; }

        protected bool Equals(User other)
        {
            return Id == other.Id;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((User) obj);
        }

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }
        
        public bool IsAdmin()
        {
            return Roles != null && Roles.Contains(RoleAdmin);
        }
    }
}