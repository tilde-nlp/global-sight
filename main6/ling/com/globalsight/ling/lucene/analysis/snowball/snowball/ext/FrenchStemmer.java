/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
// This file was generated automatically by the Snowball to Java compiler
package com.globalsight.ling.lucene.analysis.snowball.snowball.ext;
import com.globalsight.ling.lucene.analysis.snowball.snowball.SnowballProgram;
import com.globalsight.ling.lucene.analysis.snowball.snowball.Among;

/**
 * Generated class implementing code defined by a snowball script.
 */
public class FrenchStemmer extends SnowballProgram {

        private Among a_0[] = {
            new Among ( "", -1, 4, "", this),
            new Among ( "I", 0, 1, "", this),
            new Among ( "U", 0, 2, "", this),
            new Among ( "Y", 0, 3, "", this)
        };

        private Among a_1[] = {
            new Among ( "iqU", -1, 3, "", this),
            new Among ( "abl", -1, 3, "", this),
            new Among ( "I\u00E8r", -1, 4, "", this),
            new Among ( "i\u00E8r", -1, 4, "", this),
            new Among ( "eus", -1, 2, "", this),
            new Among ( "iv", -1, 1, "", this)
        };

        private Among a_2[] = {
            new Among ( "ic", -1, 2, "", this),
            new Among ( "abil", -1, 1, "", this),
            new Among ( "iv", -1, 3, "", this)
        };

        private Among a_3[] = {
            new Among ( "iqUe", -1, 1, "", this),
            new Among ( "atrice", -1, 2, "", this),
            new Among ( "ance", -1, 1, "", this),
            new Among ( "ence", -1, 5, "", this),
            new Among ( "logie", -1, 3, "", this),
            new Among ( "able", -1, 1, "", this),
            new Among ( "isme", -1, 1, "", this),
            new Among ( "euse", -1, 11, "", this),
            new Among ( "iste", -1, 1, "", this),
            new Among ( "ive", -1, 8, "", this),
            new Among ( "if", -1, 8, "", this),
            new Among ( "usion", -1, 4, "", this),
            new Among ( "ation", -1, 2, "", this),
            new Among ( "ution", -1, 4, "", this),
            new Among ( "ateur", -1, 2, "", this),
            new Among ( "iqUes", -1, 1, "", this),
            new Among ( "atrices", -1, 2, "", this),
            new Among ( "ances", -1, 1, "", this),
            new Among ( "ences", -1, 5, "", this),
            new Among ( "logies", -1, 3, "", this),
            new Among ( "ables", -1, 1, "", this),
            new Among ( "ismes", -1, 1, "", this),
            new Among ( "euses", -1, 11, "", this),
            new Among ( "istes", -1, 1, "", this),
            new Among ( "ives", -1, 8, "", this),
            new Among ( "ifs", -1, 8, "", this),
            new Among ( "usions", -1, 4, "", this),
            new Among ( "ations", -1, 2, "", this),
            new Among ( "utions", -1, 4, "", this),
            new Among ( "ateurs", -1, 2, "", this),
            new Among ( "ments", -1, 15, "", this),
            new Among ( "ements", 30, 6, "", this),
            new Among ( "issements", 31, 12, "", this),
            new Among ( "it\u00E9s", -1, 7, "", this),
            new Among ( "ment", -1, 15, "", this),
            new Among ( "ement", 34, 6, "", this),
            new Among ( "issement", 35, 12, "", this),
            new Among ( "amment", 34, 13, "", this),
            new Among ( "emment", 34, 14, "", this),
            new Among ( "aux", -1, 10, "", this),
            new Among ( "eaux", 39, 9, "", this),
            new Among ( "eux", -1, 1, "", this),
            new Among ( "it\u00E9", -1, 7, "", this)
        };

        private Among a_4[] = {
            new Among ( "ira", -1, 1, "", this),
            new Among ( "ie", -1, 1, "", this),
            new Among ( "isse", -1, 1, "", this),
            new Among ( "issante", -1, 1, "", this),
            new Among ( "i", -1, 1, "", this),
            new Among ( "irai", 4, 1, "", this),
            new Among ( "ir", -1, 1, "", this),
            new Among ( "iras", -1, 1, "", this),
            new Among ( "ies", -1, 1, "", this),
            new Among ( "\u00EEmes", -1, 1, "", this),
            new Among ( "isses", -1, 1, "", this),
            new Among ( "issantes", -1, 1, "", this),
            new Among ( "\u00EEtes", -1, 1, "", this),
            new Among ( "is", -1, 1, "", this),
            new Among ( "irais", 13, 1, "", this),
            new Among ( "issais", 13, 1, "", this),
            new Among ( "irions", -1, 1, "", this),
            new Among ( "issions", -1, 1, "", this),
            new Among ( "irons", -1, 1, "", this),
            new Among ( "issons", -1, 1, "", this),
            new Among ( "issants", -1, 1, "", this),
            new Among ( "it", -1, 1, "", this),
            new Among ( "irait", 21, 1, "", this),
            new Among ( "issait", 21, 1, "", this),
            new Among ( "issant", -1, 1, "", this),
            new Among ( "iraIent", -1, 1, "", this),
            new Among ( "issaIent", -1, 1, "", this),
            new Among ( "irent", -1, 1, "", this),
            new Among ( "issent", -1, 1, "", this),
            new Among ( "iront", -1, 1, "", this),
            new Among ( "\u00EEt", -1, 1, "", this),
            new Among ( "iriez", -1, 1, "", this),
            new Among ( "issiez", -1, 1, "", this),
            new Among ( "irez", -1, 1, "", this),
            new Among ( "issez", -1, 1, "", this)
        };

        private Among a_5[] = {
            new Among ( "a", -1, 3, "", this),
            new Among ( "era", 0, 2, "", this),
            new Among ( "asse", -1, 3, "", this),
            new Among ( "ante", -1, 3, "", this),
            new Among ( "\u00E9e", -1, 2, "", this),
            new Among ( "ai", -1, 3, "", this),
            new Among ( "erai", 5, 2, "", this),
            new Among ( "er", -1, 2, "", this),
            new Among ( "as", -1, 3, "", this),
            new Among ( "eras", 8, 2, "", this),
            new Among ( "\u00E2mes", -1, 3, "", this),
            new Among ( "asses", -1, 3, "", this),
            new Among ( "antes", -1, 3, "", this),
            new Among ( "\u00E2tes", -1, 3, "", this),
            new Among ( "\u00E9es", -1, 2, "", this),
            new Among ( "ais", -1, 3, "", this),
            new Among ( "erais", 15, 2, "", this),
            new Among ( "ions", -1, 1, "", this),
            new Among ( "erions", 17, 2, "", this),
            new Among ( "assions", 17, 3, "", this),
            new Among ( "erons", -1, 2, "", this),
            new Among ( "ants", -1, 3, "", this),
            new Among ( "\u00E9s", -1, 2, "", this),
            new Among ( "ait", -1, 3, "", this),
            new Among ( "erait", 23, 2, "", this),
            new Among ( "ant", -1, 3, "", this),
            new Among ( "aIent", -1, 3, "", this),
            new Among ( "eraIent", 26, 2, "", this),
            new Among ( "\u00E8rent", -1, 2, "", this),
            new Among ( "assent", -1, 3, "", this),
            new Among ( "eront", -1, 2, "", this),
            new Among ( "\u00E2t", -1, 3, "", this),
            new Among ( "ez", -1, 2, "", this),
            new Among ( "iez", 32, 2, "", this),
            new Among ( "eriez", 33, 2, "", this),
            new Among ( "assiez", 33, 3, "", this),
            new Among ( "erez", 32, 2, "", this),
            new Among ( "\u00E9", -1, 2, "", this)
        };

        private Among a_6[] = {
            new Among ( "e", -1, 3, "", this),
            new Among ( "I\u00E8re", 0, 2, "", this),
            new Among ( "i\u00E8re", 0, 2, "", this),
            new Among ( "ion", -1, 1, "", this),
            new Among ( "Ier", -1, 2, "", this),
            new Among ( "ier", -1, 2, "", this),
            new Among ( "\u00EB", -1, 4, "", this)
        };

        private Among a_7[] = {
            new Among ( "ell", -1, -1, "", this),
            new Among ( "eill", -1, -1, "", this),
            new Among ( "enn", -1, -1, "", this),
            new Among ( "onn", -1, -1, "", this),
            new Among ( "ett", -1, -1, "", this)
        };

        private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 130, 103, 8, 5 };

        private static final char g_keep_with_s[] = {1, 65, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

        private int I_p2;
        private int I_p1;
        private int I_pV;

        private void copy_from(FrenchStemmer other) {
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_pV = other.I_pV;
            super.copy_from(other);
        }

        private boolean r_prelude() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            // repeat, line 38
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    // goto, line 38
                    golab2: while(true)
                    {
                        v_2 = cursor;
                        lab3: do {
                            // (, line 38
                            // or, line 44
                            lab4: do {
                                v_3 = cursor;
                                lab5: do {
                                    // (, line 40
                                    if (!(in_grouping(g_v, 97, 251)))
                                    {
                                        break lab5;
                                    }
                                    // [, line 40
                                    bra = cursor;
                                    // or, line 40
                                    lab6: do {
                                        v_4 = cursor;
                                        lab7: do {
                                            // (, line 40
                                            // literal, line 40
                                            if (!(eq_s(1, "u")))
                                            {
                                                break lab7;
                                            }
                                            // ], line 40
                                            ket = cursor;
                                            if (!(in_grouping(g_v, 97, 251)))
                                            {
                                                break lab7;
                                            }
                                            // <-, line 40
                                            slice_from("U");
                                            break lab6;
                                        } while (false);
                                        cursor = v_4;
                                        lab8: do {
                                            // (, line 41
                                            // literal, line 41
                                            if (!(eq_s(1, "i")))
                                            {
                                                break lab8;
                                            }
                                            // ], line 41
                                            ket = cursor;
                                            if (!(in_grouping(g_v, 97, 251)))
                                            {
                                                break lab8;
                                            }
                                            // <-, line 41
                                            slice_from("I");
                                            break lab6;
                                        } while (false);
                                        cursor = v_4;
                                        // (, line 42
                                        // literal, line 42
                                        if (!(eq_s(1, "y")))
                                        {
                                            break lab5;
                                        }
                                        // ], line 42
                                        ket = cursor;
                                        // <-, line 42
                                        slice_from("Y");
                                    } while (false);
                                    break lab4;
                                } while (false);
                                cursor = v_3;
                                lab9: do {
                                    // (, line 45
                                    // [, line 45
                                    bra = cursor;
                                    // literal, line 45
                                    if (!(eq_s(1, "y")))
                                    {
                                        break lab9;
                                    }
                                    // ], line 45
                                    ket = cursor;
                                    if (!(in_grouping(g_v, 97, 251)))
                                    {
                                        break lab9;
                                    }
                                    // <-, line 45
                                    slice_from("Y");
                                    break lab4;
                                } while (false);
                                cursor = v_3;
                                // (, line 47
                                // literal, line 47
                                if (!(eq_s(1, "q")))
                                {
                                    break lab3;
                                }
                                // [, line 47
                                bra = cursor;
                                // literal, line 47
                                if (!(eq_s(1, "u")))
                                {
                                    break lab3;
                                }
                                // ], line 47
                                ket = cursor;
                                // <-, line 47
                                slice_from("U");
                            } while (false);
                            cursor = v_2;
                            break golab2;
                        } while (false);
                        cursor = v_2;
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }

        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            int v_4;
            // (, line 50
            I_pV = limit;
            I_p1 = limit;
            I_p2 = limit;
            // do, line 56
            v_1 = cursor;
            lab0: do {
                // (, line 56
                // or, line 57
                lab1: do {
                    v_2 = cursor;
                    lab2: do {
                        // (, line 57
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab2;
                        }
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab2;
                        }
                        // next, line 57
                        if (cursor >= limit)
                        {
                            break lab2;
                        }
                        cursor++;
                        break lab1;
                    } while (false);
                    cursor = v_2;
                    // (, line 57
                    // next, line 57
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                    // gopast, line 57
                    golab3: while(true)
                    {
                        lab4: do {
                            if (!(in_grouping(g_v, 97, 251)))
                            {
                                break lab4;
                            }
                            break golab3;
                        } while (false);
                        if (cursor >= limit)
                        {
                            break lab0;
                        }
                        cursor++;
                    }
                } while (false);
                // setmark pV, line 58
                I_pV = cursor;
            } while (false);
            cursor = v_1;
            // do, line 60
            v_4 = cursor;
            lab5: do {
                // (, line 60
                // gopast, line 61
                golab6: while(true)
                {
                    lab7: do {
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab7;
                        }
                        break golab6;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab5;
                    }
                    cursor++;
                }
                // gopast, line 61
                golab8: while(true)
                {
                    lab9: do {
                        if (!(out_grouping(g_v, 97, 251)))
                        {
                            break lab9;
                        }
                        break golab8;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab5;
                    }
                    cursor++;
                }
                // setmark p1, line 61
                I_p1 = cursor;
                // gopast, line 62
                golab10: while(true)
                {
                    lab11: do {
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab11;
                        }
                        break golab10;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab5;
                    }
                    cursor++;
                }
                // gopast, line 62
                golab12: while(true)
                {
                    lab13: do {
                        if (!(out_grouping(g_v, 97, 251)))
                        {
                            break lab13;
                        }
                        break golab12;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab5;
                    }
                    cursor++;
                }
                // setmark p2, line 62
                I_p2 = cursor;
            } while (false);
            cursor = v_4;
            return true;
        }

        private boolean r_postlude() {
            int among_var;
            int v_1;
            // repeat, line 66
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    // (, line 66
                    // [, line 68
                    bra = cursor;
                    // substring, line 68
                    among_var = find_among(a_0, 4);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    // ], line 68
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            // (, line 69
                            // <-, line 69
                            slice_from("i");
                            break;
                        case 2:
                            // (, line 70
                            // <-, line 70
                            slice_from("u");
                            break;
                        case 3:
                            // (, line 71
                            // <-, line 71
                            slice_from("y");
                            break;
                        case 4:
                            // (, line 72
                            // next, line 72
                            if (cursor >= limit)
                            {
                                break lab1;
                            }
                            cursor++;
                            break;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }

        private boolean r_RV() {
            if (!(I_pV <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_standard_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            int v_11;
            // (, line 82
            // [, line 83
            ket = cursor;
            // substring, line 83
            among_var = find_among_b(a_3, 43);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 83
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 87
                    // call R2, line 87
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 87
                    slice_del();
                    break;
                case 2:
                    // (, line 90
                    // call R2, line 90
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 90
                    slice_del();
                    // try, line 91
                    v_1 = limit - cursor;
                    lab0: do {
                        // (, line 91
                        // [, line 91
                        ket = cursor;
                        // literal, line 91
                        if (!(eq_s_b(2, "ic")))
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        // ], line 91
                        bra = cursor;
                        // or, line 91
                        lab1: do {
                            v_2 = limit - cursor;
                            lab2: do {
                                // (, line 91
                                // call R2, line 91
                                if (!r_R2())
                                {
                                    break lab2;
                                }
                                // delete, line 91
                                slice_del();
                                break lab1;
                            } while (false);
                            cursor = limit - v_2;
                            // <-, line 91
                            slice_from("iqU");
                        } while (false);
                    } while (false);
                    break;
                case 3:
                    // (, line 95
                    // call R2, line 95
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 95
                    slice_from("log");
                    break;
                case 4:
                    // (, line 98
                    // call R2, line 98
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 98
                    slice_from("u");
                    break;
                case 5:
                    // (, line 101
                    // call R2, line 101
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 101
                    slice_from("ent");
                    break;
                case 6:
                    // (, line 104
                    // call RV, line 105
                    if (!r_RV())
                    {
                        return false;
                    }
                    // delete, line 105
                    slice_del();
                    // try, line 106
                    v_3 = limit - cursor;
                    lab3: do {
                        // (, line 106
                        // [, line 107
                        ket = cursor;
                        // substring, line 107
                        among_var = find_among_b(a_1, 6);
                        if (among_var == 0)
                        {
                            cursor = limit - v_3;
                            break lab3;
                        }
                        // ], line 107
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_3;
                                break lab3;
                            case 1:
                                // (, line 108
                                // call R2, line 108
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 108
                                slice_del();
                                // [, line 108
                                ket = cursor;
                                // literal, line 108
                                if (!(eq_s_b(2, "at")))
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // ], line 108
                                bra = cursor;
                                // call R2, line 108
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 108
                                slice_del();
                                break;
                            case 2:
                                // (, line 109
                                // or, line 109
                                lab4: do {
                                    v_4 = limit - cursor;
                                    lab5: do {
                                        // (, line 109
                                        // call R2, line 109
                                        if (!r_R2())
                                        {
                                            break lab5;
                                        }
                                        // delete, line 109
                                        slice_del();
                                        break lab4;
                                    } while (false);
                                    cursor = limit - v_4;
                                    // (, line 109
                                    // call R1, line 109
                                    if (!r_R1())
                                    {
                                        cursor = limit - v_3;
                                        break lab3;
                                    }
                                    // <-, line 109
                                    slice_from("eux");
                                } while (false);
                                break;
                            case 3:
                                // (, line 111
                                // call R2, line 111
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 111
                                slice_del();
                                break;
                            case 4:
                                // (, line 113
                                // call RV, line 113
                                if (!r_RV())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // <-, line 113
                                slice_from("i");
                                break;
                        }
                    } while (false);
                    break;
                case 7:
                    // (, line 119
                    // call R2, line 120
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 120
                    slice_del();
                    // try, line 121
                    v_5 = limit - cursor;
                    lab6: do {
                        // (, line 121
                        // [, line 122
                        ket = cursor;
                        // substring, line 122
                        among_var = find_among_b(a_2, 3);
                        if (among_var == 0)
                        {
                            cursor = limit - v_5;
                            break lab6;
                        }
                        // ], line 122
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_5;
                                break lab6;
                            case 1:
                                // (, line 123
                                // or, line 123
                                lab7: do {
                                    v_6 = limit - cursor;
                                    lab8: do {
                                        // (, line 123
                                        // call R2, line 123
                                        if (!r_R2())
                                        {
                                            break lab8;
                                        }
                                        // delete, line 123
                                        slice_del();
                                        break lab7;
                                    } while (false);
                                    cursor = limit - v_6;
                                    // <-, line 123
                                    slice_from("abl");
                                } while (false);
                                break;
                            case 2:
                                // (, line 124
                                // or, line 124
                                lab9: do {
                                    v_7 = limit - cursor;
                                    lab10: do {
                                        // (, line 124
                                        // call R2, line 124
                                        if (!r_R2())
                                        {
                                            break lab10;
                                        }
                                        // delete, line 124
                                        slice_del();
                                        break lab9;
                                    } while (false);
                                    cursor = limit - v_7;
                                    // <-, line 124
                                    slice_from("iqU");
                                } while (false);
                                break;
                            case 3:
                                // (, line 125
                                // call R2, line 125
                                if (!r_R2())
                                {
                                    cursor = limit - v_5;
                                    break lab6;
                                }
                                // delete, line 125
                                slice_del();
                                break;
                        }
                    } while (false);
                    break;
                case 8:
                    // (, line 131
                    // call R2, line 132
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 132
                    slice_del();
                    // try, line 133
                    v_8 = limit - cursor;
                    lab11: do {
                        // (, line 133
                        // [, line 133
                        ket = cursor;
                        // literal, line 133
                        if (!(eq_s_b(2, "at")))
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // ], line 133
                        bra = cursor;
                        // call R2, line 133
                        if (!r_R2())
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // delete, line 133
                        slice_del();
                        // [, line 133
                        ket = cursor;
                        // literal, line 133
                        if (!(eq_s_b(2, "ic")))
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // ], line 133
                        bra = cursor;
                        // or, line 133
                        lab12: do {
                            v_9 = limit - cursor;
                            lab13: do {
                                // (, line 133
                                // call R2, line 133
                                if (!r_R2())
                                {
                                    break lab13;
                                }
                                // delete, line 133
                                slice_del();
                                break lab12;
                            } while (false);
                            cursor = limit - v_9;
                            // <-, line 133
                            slice_from("iqU");
                        } while (false);
                    } while (false);
                    break;
                case 9:
                    // (, line 135
                    // <-, line 135
                    slice_from("eau");
                    break;
                case 10:
                    // (, line 136
                    // call R1, line 136
                    if (!r_R1())
                    {
                        return false;
                    }
                    // <-, line 136
                    slice_from("al");
                    break;
                case 11:
                    // (, line 138
                    // or, line 138
                    lab14: do {
                        v_10 = limit - cursor;
                        lab15: do {
                            // (, line 138
                            // call R2, line 138
                            if (!r_R2())
                            {
                                break lab15;
                            }
                            // delete, line 138
                            slice_del();
                            break lab14;
                        } while (false);
                        cursor = limit - v_10;
                        // (, line 138
                        // call R1, line 138
                        if (!r_R1())
                        {
                            return false;
                        }
                        // <-, line 138
                        slice_from("eux");
                    } while (false);
                    break;
                case 12:
                    // (, line 141
                    // call R1, line 141
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!(out_grouping_b(g_v, 97, 251)))
                    {
                        return false;
                    }
                    // delete, line 141
                    slice_del();
                    break;
                case 13:
                    // (, line 146
                    // call RV, line 146
                    if (!r_RV())
                    {
                        return false;
                    }
                    // fail, line 146
                    // (, line 146
                    // <-, line 146
                    slice_from("ant");
                    return false;
                case 14:
                    // (, line 147
                    // call RV, line 147
                    if (!r_RV())
                    {
                        return false;
                    }
                    // fail, line 147
                    // (, line 147
                    // <-, line 147
                    slice_from("ent");
                    return false;
                case 15:
                    // (, line 149
                    // test, line 149
                    v_11 = limit - cursor;
                    // (, line 149
                    if (!(in_grouping_b(g_v, 97, 251)))
                    {
                        return false;
                    }
                    // call RV, line 149
                    if (!r_RV())
                    {
                        return false;
                    }
                    cursor = limit - v_11;
                    // fail, line 149
                    // (, line 149
                    // delete, line 149
                    slice_del();
                    return false;
            }
            return true;
        }

        private boolean r_i_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            // setlimit, line 154
            v_1 = limit - cursor;
            // tomark, line 154
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            // (, line 154
            // [, line 155
            ket = cursor;
            // substring, line 155
            among_var = find_among_b(a_4, 35);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            // ], line 155
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    // (, line 161
                    if (!(out_grouping_b(g_v, 97, 251)))
                    {
                        limit_backward = v_2;
                        return false;
                    }
                    // delete, line 161
                    slice_del();
                    break;
            }
            limit_backward = v_2;
            return true;
        }

        private boolean r_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            // setlimit, line 165
            v_1 = limit - cursor;
            // tomark, line 165
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            // (, line 165
            // [, line 166
            ket = cursor;
            // substring, line 166
            among_var = find_among_b(a_5, 38);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            // ], line 166
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    // (, line 168
                    // call R2, line 168
                    if (!r_R2())
                    {
                        limit_backward = v_2;
                        return false;
                    }
                    // delete, line 168
                    slice_del();
                    break;
                case 2:
                    // (, line 176
                    // delete, line 176
                    slice_del();
                    break;
                case 3:
                    // (, line 181
                    // delete, line 181
                    slice_del();
                    // try, line 182
                    v_3 = limit - cursor;
                    lab0: do {
                        // (, line 182
                        // [, line 182
                        ket = cursor;
                        // literal, line 182
                        if (!(eq_s_b(1, "e")))
                        {
                            cursor = limit - v_3;
                            break lab0;
                        }
                        // ], line 182
                        bra = cursor;
                        // delete, line 182
                        slice_del();
                    } while (false);
                    break;
            }
            limit_backward = v_2;
            return true;
        }

        private boolean r_residual_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            // (, line 189
            // try, line 190
            v_1 = limit - cursor;
            lab0: do {
                // (, line 190
                // [, line 190
                ket = cursor;
                // literal, line 190
                if (!(eq_s_b(1, "s")))
                {
                    cursor = limit - v_1;
                    break lab0;
                }
                // ], line 190
                bra = cursor;
                // test, line 190
                v_2 = limit - cursor;
                if (!(out_grouping_b(g_keep_with_s, 97, 232)))
                {
                    cursor = limit - v_1;
                    break lab0;
                }
                cursor = limit - v_2;
                // delete, line 190
                slice_del();
            } while (false);
            // setlimit, line 191
            v_3 = limit - cursor;
            // tomark, line 191
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_4 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_3;
            // (, line 191
            // [, line 192
            ket = cursor;
            // substring, line 192
            among_var = find_among_b(a_6, 7);
            if (among_var == 0)
            {
                limit_backward = v_4;
                return false;
            }
            // ], line 192
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_4;
                    return false;
                case 1:
                    // (, line 193
                    // call R2, line 193
                    if (!r_R2())
                    {
                        limit_backward = v_4;
                        return false;
                    }
                    // or, line 193
                    lab1: do {
                        v_5 = limit - cursor;
                        lab2: do {
                            // literal, line 193
                            if (!(eq_s_b(1, "s")))
                            {
                                break lab2;
                            }
                            break lab1;
                        } while (false);
                        cursor = limit - v_5;
                        // literal, line 193
                        if (!(eq_s_b(1, "t")))
                        {
                            limit_backward = v_4;
                            return false;
                        }
                    } while (false);
                    // delete, line 193
                    slice_del();
                    break;
                case 2:
                    // (, line 195
                    // <-, line 195
                    slice_from("i");
                    break;
                case 3:
                    // (, line 196
                    // delete, line 196
                    slice_del();
                    break;
                case 4:
                    // (, line 197
                    // literal, line 197
                    if (!(eq_s_b(2, "gu")))
                    {
                        limit_backward = v_4;
                        return false;
                    }
                    // delete, line 197
                    slice_del();
                    break;
            }
            limit_backward = v_4;
            return true;
        }

        private boolean r_un_double() {
            int v_1;
            // (, line 202
            // test, line 203
            v_1 = limit - cursor;
            // among, line 203
            if (find_among_b(a_7, 5) == 0)
            {
                return false;
            }
            cursor = limit - v_1;
            // [, line 203
            ket = cursor;
            // next, line 203
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            // ], line 203
            bra = cursor;
            // delete, line 203
            slice_del();
            return true;
        }

        private boolean r_un_accent() {
            int v_3;
            // (, line 206
            // atleast, line 207
            {
                int v_1 = 1;
                // atleast, line 207
                replab0: while(true)
                {
                    lab1: do {
                        if (!(out_grouping_b(g_v, 97, 251)))
                        {
                            break lab1;
                        }
                        v_1--;
                        continue replab0;
                    } while (false);
                    break replab0;
                }
                if (v_1 > 0)
                {
                    return false;
                }
            }
            // [, line 208
            ket = cursor;
            // or, line 208
            lab2: do {
                v_3 = limit - cursor;
                lab3: do {
                    // literal, line 208
                    if (!(eq_s_b(1, "\u00E9")))
                    {
                        break lab3;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                // literal, line 208
                if (!(eq_s_b(1, "\u00E8")))
                {
                    return false;
                }
            } while (false);
            // ], line 208
            bra = cursor;
            // <-, line 208
            slice_from("e");
            return true;
        }

        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            int v_11;
            // (, line 212
            // do, line 214
            v_1 = cursor;
            lab0: do {
                // call prelude, line 214
                if (!r_prelude())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            // do, line 215
            v_2 = cursor;
            lab1: do {
                // call mark_regions, line 215
                if (!r_mark_regions())
                {
                    break lab1;
                }
            } while (false);
            cursor = v_2;
            // backwards, line 216
            limit_backward = cursor; cursor = limit;
            // (, line 216
            // do, line 218
            v_3 = limit - cursor;
            lab2: do {
                // (, line 218
                // or, line 228
                lab3: do {
                    v_4 = limit - cursor;
                    lab4: do {
                        // (, line 219
                        // and, line 224
                        v_5 = limit - cursor;
                        // (, line 220
                        // or, line 220
                        lab5: do {
                            v_6 = limit - cursor;
                            lab6: do {
                                // call standard_suffix, line 220
                                if (!r_standard_suffix())
                                {
                                    break lab6;
                                }
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            lab7: do {
                                // call i_verb_suffix, line 221
                                if (!r_i_verb_suffix())
                                {
                                    break lab7;
                                }
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            // call verb_suffix, line 222
                            if (!r_verb_suffix())
                            {
                                break lab4;
                            }
                        } while (false);
                        cursor = limit - v_5;
                        // try, line 225
                        v_7 = limit - cursor;
                        lab8: do {
                            // (, line 225
                            // [, line 225
                            ket = cursor;
                            // or, line 225
                            lab9: do {
                                v_8 = limit - cursor;
                                lab10: do {
                                    // (, line 225
                                    // literal, line 225
                                    if (!(eq_s_b(1, "Y")))
                                    {
                                        break lab10;
                                    }
                                    // ], line 225
                                    bra = cursor;
                                    // <-, line 225
                                    slice_from("i");
                                    break lab9;
                                } while (false);
                                cursor = limit - v_8;
                                // (, line 226
                                // literal, line 226
                                if (!(eq_s_b(1, "\u00E7")))
                                {
                                    cursor = limit - v_7;
                                    break lab8;
                                }
                                // ], line 226
                                bra = cursor;
                                // <-, line 226
                                slice_from("c");
                            } while (false);
                        } while (false);
                        break lab3;
                    } while (false);
                    cursor = limit - v_4;
                    // call residual_suffix, line 229
                    if (!r_residual_suffix())
                    {
                        break lab2;
                    }
                } while (false);
            } while (false);
            cursor = limit - v_3;
            // do, line 234
            v_9 = limit - cursor;
            lab11: do {
                // call un_double, line 234
                if (!r_un_double())
                {
                    break lab11;
                }
            } while (false);
            cursor = limit - v_9;
            // do, line 235
            v_10 = limit - cursor;
            lab12: do {
                // call un_accent, line 235
                if (!r_un_accent())
                {
                    break lab12;
                }
            } while (false);
            cursor = limit - v_10;
            cursor = limit_backward;            // do, line 237
            v_11 = cursor;
            lab13: do {
                // call postlude, line 237
                if (!r_postlude())
                {
                    break lab13;
                }
            } while (false);
            cursor = v_11;
            return true;
        }

}

