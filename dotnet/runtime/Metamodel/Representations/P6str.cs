﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that we use (for now) for dealing with
    /// strings.
    /// </summary>
    public sealed class P6str : Representation
    {
        /// <summary>
        /// This is how the boxed form of a P6str looks.
        /// </summary>
        internal sealed class Instance : RakudoObject
        {
            public string Value;
            public Instance(SharedTable STable)
            {
                this.STable = STable;
            }
        }

        /// <summary>
        /// Create a new type object.
        /// </summary>
        /// <param name="MetaPackage"></param>
        /// <returns></returns>
        public override RakudoObject type_object_for(ThreadContext TC, RakudoObject MetaPackage)
        {
            var STable = new SharedTable();
            STable.HOW = MetaPackage;
            STable.REPR = this;
            STable.WHAT = new Instance(STable);
            return STable.WHAT;
        }

        /// <summary>
        /// Creates an instance of the type with the given type object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public override RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
        {
            var Object = new Instance(WHAT.STable);
            Object.Value = "";
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public override bool defined(ThreadContext TC, RakudoObject Obj)
        {
            return ((Instance)Obj).Value != null;
        }

        public override RakudoObject get_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override void bind_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override void bind_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override int hint_for(ThreadContext TC, RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }

        public override void set_int(ThreadContext TC, RakudoObject Object, int Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native int");
        }

        public override int get_int(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native int");
        }

        public override void set_num(ThreadContext TC, RakudoObject Object, double Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native num");
        }

        public override double get_num(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native num");
        }

        public override void set_str(ThreadContext TC, RakudoObject Object, string Value)
        {
            ((Instance)Object).Value = Value;
        }

        public override string get_str(ThreadContext TC, RakudoObject Object)
        {
            return ((Instance)Object).Value;
        }
    }
}
