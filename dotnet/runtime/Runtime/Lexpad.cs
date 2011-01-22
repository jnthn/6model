using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Represents a lexpad - either the static version or the dynamic
    /// one.
    /// </summary>
    public struct Lexpad
    {
        /// <summary>
        /// This is the slot mapping, allocating names to slots. All the
        /// dynamic variants of a lexpad will share this with the static
        /// lexpad.
        /// </summary>
        public Dictionary<string, int> SlotMapping;

        /// <summary>
        /// The storage associated with the lexpad.
        /// </summary>
        public RakudoObject[] Storage;

        /// <summary>
        /// Creates a new static lexpad with the given names all
        /// allocated slots, in the order they appear in the array.
        /// </summary>
        /// <param name="SlotNames"></param>
        public Lexpad(string[] SlotNames)
        {
            SlotMapping = new Dictionary<string, int>(SlotNames.Length);
            int Slot = 0;
            foreach (var Name in SlotNames)
                SlotMapping.Add(Name, Slot++);
            Storage = new RakudoObject[SlotNames.Length];
        }

        /// <summary>
        /// Looks up a lexical by name.
        /// </summary>
        /// <param name="Name"></param>
        /// <returns></returns>
        public RakudoObject GetByName(string Name)
        {
            return Storage[SlotMapping[Name]];
        }

        /// <summary>
        /// Sets a lexical by name.
        /// </summary>
        /// <param name="Name"></param>
        /// <returns></returns>
        public RakudoObject SetByName(string Name, RakudoObject Value)
        {
            Storage[SlotMapping[Name]] = Value;
            return Value;
        }

        /// <summary>
        /// Extends the lexpad with an extra slot.
        /// </summary>
        /// <param name="Name"></param>
        public void Extend(string[] Names)
        {
            // Add new entry to the mapping. Note that we re-build
            // it and make it unique to this lexpad now, even if it
            // was shared before, but add the extra entry.
            SlotMapping = new Dictionary<string, int>(SlotMapping);
            int NewSlot = Storage.Length;
            foreach (var Name in Names)
                SlotMapping.Add(Name, NewSlot++);

            // Reallocate enlarged storage.
            var NewStorage = new RakudoObject[Storage.Length + Names.Length];
            for (int i = 0; i < Storage.Length; i++)
                NewStorage[i] = Storage[i];
            Storage = NewStorage;
        }
    }
}
