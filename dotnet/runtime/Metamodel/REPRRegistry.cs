using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel
{
    /// <summary>
    /// This is our central registry of representations.
    /// </summary>
    public static class REPRRegistry
    {
        /// <summary>
        /// ID indexed list.
        /// </summary>
        private static List<IRepresentation> Registry = new List<IRepresentation>();

        /// <summary>
        /// Maps names to IDs so we can do named lookups too.
        /// </summary>
        private static Dictionary<string, int> NamedToIDMapper = new Dictionary<string, int>();

        /// <summary>
        /// Adds a representation to the registry and returns its
        /// registered ID.
        /// </summary>
        /// <param name="Name"></param>
        /// <param name="REPR"></param>
        /// <returns></returns>
        public static int register_REPR(string Name, IRepresentation REPR)
        {
            Registry.Add(REPR);
            var ID = Registry.Count - 1;
            NamedToIDMapper.Add(Name, ID);
            return ID;
        }

        /// <summary>
        /// Gets a representation by ID.
        /// </summary>
        /// <param name="ID"></param>
        /// <returns></returns>
        public static IRepresentation get_REPR_by_id(int ID)
        {
            return Registry[ID];
        }

        /// <summary>
        /// Gets a representation by name.
        /// </summary>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static IRepresentation get_REPR_by_name(string Name)
        {
            return Registry[NamedToIDMapper[Name]];
        }
    }
}
