/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.metalava

import org.intellij.lang.annotations.Language
import org.junit.Test

class ApiFromTextTest : DriverTest() {

    @Test
    fun `Loading a signature file and writing the API back out`() {
        val source = """
                package test.pkg {
                  public class MyTest {
                    ctor public MyTest();
                    method public int clamp(int);
                    method public java.lang.Double convert(java.lang.Float);
                    field public static final java.lang.String ANY_CURSOR_ITEM_TYPE = "vnd.android.cursor.item/*";
                    field public java.lang.Number myNumber;
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Handle lambas as default values`() {
        val source = """
            // Signature format: $SIGNATURE_FORMAT
            package androidx.collection {
              public final class LruCacheKt {
                ctor public LruCacheKt();
                method public static <K, V> androidx.collection.LruCache<K,V> lruCache(int maxSize, kotlin.jvm.functions.Function2<? super K,? super V,java.lang.Integer> sizeOf = { _, _ -> 1 }, kotlin.jvm.functions.Function1<? super K,? extends V> create = { null as V? }, kotlin.jvm.functions.Function4<? super java.lang.Boolean,? super K,? super V,? super V,kotlin.Unit> onEntryRemoved = { _, _, _, _ -> });
              }
            }
        """

        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            signatureSource = source,
            includeSignatureVersion = true,
            api = source
        )
    }

    @Test
    fun `Handle enum constants as default values`() {
        val source = """
            // Signature format: $SIGNATURE_FORMAT
            package test.pkg {
              public final class Foo {
                ctor public Foo();
                method public android.graphics.Bitmap? drawToBitmap(android.view.View, android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888);
                method public void emptyLambda(kotlin.jvm.functions.Function0<kotlin.Unit> sizeOf = {});
                method public void method1(int p = 42, Integer? int2 = null, int p1 = 42, String str = "hello world", java.lang.String... args);
                method public void method2(int p, int int2 = (2 * int) * some.other.pkg.Constants.Misc.SIZE);
                method public void method3(String str, int p, int int2 = double(int) + str.length);
                field public static final test.pkg.Foo.Companion! Companion;
              }
              public static final class Foo.Companion {
                method public int double(int p);
                method public void print(test.pkg.Foo foo = test.pkg.Foo());
              }
              public final class LruCacheKt {
                ctor public LruCacheKt();
                method public static <K, V> android.util.LruCache<K,V> lruCache(int maxSize, kotlin.jvm.functions.Function2<? super K,? super V,java.lang.Integer> sizeOf = { _, _ -> 1 }, kotlin.jvm.functions.Function1<? super K,? extends V> create = { (V)null }, kotlin.jvm.functions.Function4<? super java.lang.Boolean,? super K,? super V,? super V,kotlin.Unit> onEntryRemoved = { _, _, _, _ ->  });
              }
            }
            """

        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            signatureSource = source,
            includeSignatureVersion = true,
            api = source
        )
    }

    @Test
    fun `Handle complex expressions as default values`() {
        val source = """
            // Signature format: $SIGNATURE_FORMAT
            package androidx.paging {
              public final class PagedListConfigKt {
                ctor public PagedListConfigKt();
                method public static androidx.paging.PagedList.Config Config(int pageSize, int prefetchDistance = pageSize, boolean enablePlaceholders = true, int initialLoadSizeHint = pageSize * PagedList.Config.Builder.DEFAULT_INITIAL_PAGE_MULTIPLIER, int maxSize = PagedList.Config.MAX_SIZE_UNBOUNDED);
              }
              public final class PagedListKt {
                ctor public PagedListKt();
                method public static <Key, Value> androidx.paging.PagedList<Value> PagedList(androidx.paging.DataSource<Key,Value> dataSource, androidx.paging.PagedList.Config config, java.util.concurrent.Executor notifyExecutor, java.util.concurrent.Executor fetchExecutor, androidx.paging.PagedList.BoundaryCallback<Value>? boundaryCallback = null, Key? initialKey = null);
              }
            }
            package test.pkg {
              public final class Foo {
                ctor public Foo();
                method public void method1(int p = 42, Integer? int2 = null, int p1 = 42, String str = "hello world", java.lang.String... args);
                method public void method2(int p, int int2 = (2 * int) * some.other.pkg.Constants.Misc.SIZE);
                method public void method3(str: String = "unbalanced), string", str2: String = ",");
              }
            }
        """

        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            signatureSource = source,
            includeSignatureVersion = true,
            api = source
        )
    }

    @Test
    fun `Annotation signatures requiring more complicated token matching`() {
        val source = """
                package test {
                  public class MyTest {
                    method @RequiresPermission(value="android.permission.AUTHENTICATE_ACCOUNTS", apis="..22") public boolean addAccountExplicitly(android.accounts.Account, String, android.os.Bundle);
                    method @CheckResult(suggest="#enforceCallingOrSelfPermission(String,\"foo\",String)") public abstract int checkCallingOrSelfPermission(@NonNull String);
                    method @RequiresPermission(anyOf={"android.permission.MANAGE_ACCOUNTS", "android.permission.USE_CREDENTIALS"}, apis="..22") public void invalidateAuthToken(String, String);
                  }
                }
                """
        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Multiple extends`() {
        val source = """
                package test {
                  public static interface PickConstructors extends test.pkg.PickConstructors.AutoCloseable {
                  }
                  public interface XmlResourceParser extends org.xmlpull.v1.XmlPullParser android.util.AttributeSet java.lang.AutoCloseable {
                    method public void close();
                  }
                }
                """
        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Native and strictfp keywords`() {
        val source = """
                package test.pkg {
                  public class MyTest {
                    method public native float dotWithNormal(float, float, float);
                    method public static strictfp double toDegrees(double);
                  }
                }
                """
        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Type use annotations`() {
        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = """
                package test.pkg {
                  public class MyTest {
                    method public static int codePointAt(char @NonNull [], int);
                    method @NonNull public java.util.Set<java.util.Map.@NonNull Entry<K,V>> entrySet();
                    method @NonNull public java.lang.annotation.@NonNull Annotation @NonNull [] getAnnotations();
                    method @NonNull public abstract java.lang.annotation.@NonNull Annotation @NonNull [] @NonNull [] getParameterAnnotations();
                    method @NonNull public @NonNull String @NonNull [] split(@NonNull String, int);
                    method public static char @NonNull [] toChars(int);
                  }
                }
                """,
            api = """
                package test.pkg {
                  public class MyTest {
                    method public static int codePointAt(char @NonNull [], int);
                    method @NonNull public java.util.Set<java.util.Map.@NonNull Entry<K,V>> entrySet();
                    method @NonNull public java.lang.annotation.Annotation @NonNull [] getAnnotations();
                    method @NonNull public abstract java.lang.annotation.Annotation @NonNull [] @NonNull [] getParameterAnnotations();
                    method @NonNull public String @NonNull [] split(@NonNull String, int);
                    method public static char @NonNull [] toChars(int);
                  }
                }
            """
        )
    }

    @Test
    fun `Vararg modifier`() {
        val source = """
                package test.pkg {
                  public final class Foo {
                    ctor public Foo();
                    method public void error(int p = "42", Integer int2 = "null", int p1 = "42", vararg String args);
                  }
                }
                """
        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = source
        )
    }

    @Test
    fun `Infer fully qualified names from shorter names`() {
        check(
            compatibilityMode = true,
            extraArguments = arrayOf("--annotations-in-signatures"),
            signatureSource = """
                package test.pkg {
                  public class MyTest {
                    ctor public MyTest();
                    method public int clamp(int);
                    method public double convert(@Nullable Float, byte[], Iterable<java.io.File>);
                  }
                }
                """,
            api = """
                package test.pkg {
                  public class MyTest {
                    ctor public MyTest();
                    method public int clamp(int);
                    method public double convert(@androidx.annotation.Nullable java.lang.Float, byte[], java.lang.Iterable<java.io.File>);
                  }
                }
                """
        )
    }

    @Test
    fun `Loading a signature file with alternate modifier order`() {
        // Regression test for https://github.com/android/android-ktx/issues/242
        val source = """
                package test.pkg {
                  deprecated public class MyTest {
                    ctor deprecated public Foo(int, int);
                    method deprecated public static final void edit(android.content.SharedPreferences, kotlin.jvm.functions.Function1<? super android.content.SharedPreferences.Editor,kotlin.Unit> action);
                    field deprecated public static java.util.List<java.lang.String> LIST;
                  }
                }
                """
        check(
            compatibilityMode = true,
            signatureSource = source,
            api = """
                package test.pkg {
                  public deprecated class MyTest {
                    ctor public deprecated MyTest(int, int);
                    method public static final deprecated void edit(android.content.SharedPreferences, kotlin.jvm.functions.Function1<? super android.content.SharedPreferences.Editor,kotlin.Unit> action);
                    field public static deprecated java.util.List<java.lang.String> LIST;
                  }
                }
                """
        )
    }

    @Test
    fun `Test generics, superclasses and interfaces`() {
        val source = """
            package a.b.c {
              public abstract interface MyStream<T, S extends a.b.c.MyStream<T, S>> {
              }
            }
            package test.pkg {
              public final class Foo extends java.lang.Enum {
                ctor public Foo(int);
                ctor public Foo(int, int);
                method public static test.pkg.Foo valueOf(java.lang.String);
                method public static final test.pkg.Foo[] values();
                enum_constant public static final test.pkg.Foo A;
                enum_constant public static final test.pkg.Foo B;
              }
              public abstract interface MyBaseInterface {
              }
              public abstract interface MyInterface<T> implements test.pkg.MyBaseInterface {
              }
              public abstract interface MyInterface2<T extends java.lang.Number> implements test.pkg.MyBaseInterface {
              }
              public static abstract class MyInterface2.Range<T extends java.lang.Comparable<? super T>> {
                ctor public MyInterface2.Range();
              }
              public static class MyInterface2.TtsSpan<C extends test.pkg.MyInterface<?>> {
                ctor public MyInterface2.TtsSpan();
              }
              public final class Test<T> {
                ctor public Test();
                method public abstract <T extends java.util.Collection<java.lang.String>> T addAllTo(T);
                method public static <T & java.lang.Comparable<? super T>> T max(java.util.Collection<? extends T>);
                method public <X extends java.lang.Throwable> T orElseThrow(java.util.function.Supplier<? extends X>) throws java.lang.Throwable;
                field public static java.util.List<java.lang.String> LIST;
              }
            }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Test constants`() {
        val source = """
                package test.pkg {
                  public class Foo2 {
                    ctor public Foo2();
                    field public static final java.lang.String GOOD_IRI_CHAR = "a-zA-Z0-9\u00a0-\ud7ff\uf900-\ufdcf\ufdf0-\uffef";
                    field public static final char HEX_INPUT = 61184; // 0xef00 '\uef00'
                    field protected int field00;
                    field public static final boolean field01 = true;
                    field public static final int field02 = 42; // 0x2a
                    field public static final long field03 = 42L; // 0x2aL
                    field public static final short field04 = 5; // 0x5
                    field public static final byte field05 = 5; // 0x5
                    field public static final char field06 = 99; // 0x0063 'c'
                    field public static final float field07 = 98.5f;
                    field public static final double field08 = 98.5;
                    field public static final java.lang.String field09 = "String with \"escapes\" and \u00a9...";
                    field public static final double field10 = (0.0/0.0);
                    field public static final double field11 = (1.0/0.0);
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Test inner classes`() {
        val source = """
                package test.pkg {
                  public abstract class Foo {
                    ctor public Foo();
                    method public static final deprecated synchronized void method1();
                    method public static final deprecated synchronized void method2();
                  }
                  protected static final deprecated class Foo.Inner1 {
                    ctor protected Foo.Inner1();
                  }
                  protected static abstract deprecated class Foo.Inner2 {
                    ctor protected Foo.Inner2();
                  }
                  protected static abstract deprecated interface Foo.Inner3 {
                    method public default void method3();
                    method public static void method4(int);
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Test throws`() {
        val source = """
                package test.pkg {
                  public final class Test<T> {
                    ctor public Test();
                    method public <X extends java.lang.Throwable> T orElseThrow(java.util.function.Supplier<? extends X>) throws java.lang.Throwable;
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Loading a signature file with annotations on classes, fields, methods and parameters`() {
        @Language("TEXT")
        val source = """
                package test.pkg {
                  @androidx.annotation.UiThread public class MyTest {
                    ctor public MyTest();
                    method @androidx.annotation.IntRange(from=10, to=20) public int clamp(int);
                    method public java.lang.Double? convert(java.lang.Float myPublicName);
                    field public java.lang.Number? myNumber;
                  }
                }
                """

        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            omitCommonPackages = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Enums and annotations`() {
        // In non-compat mode we write interfaces out as "@interface" (instead of abstract class)
        // and similarly for enums we write "enum" instead of "class extends java.lang.Enum".
        // Make sure we can also read this back in.
        val source = """
                package android.annotation {
                  public @interface SuppressLint {
                    method public abstract String[] value();
                  }
                }
                package test.pkg {
                  public enum Foo {
                    enum_constant public static final test.pkg.Foo A;
                    enum_constant public static final test.pkg.Foo B;
                  }
                }
                """

        check(
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Enums and annotations exported to compat`() {
        val source = """
                package android.annotation {
                  public @interface SuppressLint {
                  }
                }
                package test.pkg {
                  public final enum Foo {
                    enum_constant public static final test.pkg.Foo A;
                    enum_constant public static final test.pkg.Foo B;
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = """
                package android.annotation {
                  public abstract class SuppressLint implements java.lang.annotation.Annotation {
                  }
                }
                package test.pkg {
                  public final class Foo extends java.lang.Enum {
                    enum_constant public static final test.pkg.Foo A;
                    enum_constant public static final test.pkg.Foo B;
                  }
                }
                """
        )
    }

    @Test
    fun `Sort throws list by full name`() {
        check(
            compatibilityMode = true,
            signatureSource = """
                    package android.accounts {
                      public abstract interface AccountManagerFuture<V> {
                        method public abstract boolean cancel(boolean);
                        method public abstract V getResult() throws android.accounts.OperationCanceledException, java.io.IOException, android.accounts.AuthenticatorException;
                        method public abstract V getResult(long, java.util.concurrent.TimeUnit) throws android.accounts.OperationCanceledException, java.io.IOException, android.accounts.AuthenticatorException;
                        method public abstract boolean isCancelled();
                        method public abstract boolean isDone();
                      }
                    }
                    """,
            api = """
                    package android.accounts {
                      public abstract interface AccountManagerFuture<V> {
                        method public abstract boolean cancel(boolean);
                        method public abstract V getResult() throws android.accounts.AuthenticatorException, java.io.IOException, android.accounts.OperationCanceledException;
                        method public abstract V getResult(long, java.util.concurrent.TimeUnit) throws android.accounts.AuthenticatorException, java.io.IOException, android.accounts.OperationCanceledException;
                        method public abstract boolean isCancelled();
                        method public abstract boolean isDone();
                      }
                    }
                    """
        )
    }

    @Test
    fun `Loading a signature file with default values`() {
        @Language("TEXT")
        val source = """
                package test.pkg {
                  public final class Foo {
                    ctor public Foo();
                    method public final void error(int p = 42, java.lang.Integer? int2 = null);
                  }
                  public class Foo2 {
                    ctor public Foo2();
                    method public void foo(java.lang.String! = null, java.lang.String! = "(Hello) World", int = 42);
                  }
                }
                """

        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            omitCommonPackages = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Signatures with default annotation method values`() {
        val source = """
                package libcore.util {
                  public @interface NonNull {
                    method public abstract int from() default java.lang.Integer.MIN_VALUE;
                    method public abstract double fromWithCast() default (double)java.lang.Float.NEGATIVE_INFINITY;
                    method public abstract String! myString() default "This is a \"string\"";
                    method public abstract int to() default java.lang.Integer.MAX_VALUE;
                  }
                }
                """

        check(
            inputKotlinStyleNulls = true,
            compatibilityMode = false,
            signatureSource = source,
            api = source
        )
    }

    @Test
    fun `Signatures with many annotations`() {
        val source = """
            package libcore.util {
              @java.lang.annotation.Documented @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE) public @interface NonNull {
                method public abstract int from() default java.lang.Integer.MIN_VALUE;
                method public abstract int to() default java.lang.Integer.MAX_VALUE;
              }
            }
            package test.pkg {
              public class Test {
                ctor public Test();
                method @NonNull public Object compute();
              }
            }
        """

        check(
            compatibilityMode = false,
            signatureSource = source,
            outputKotlinStyleNulls = false,
            api = source
        )
    }

    @Test
    fun `Kotlin Properties`() {
        val source = """
                package test.pkg {
                  public final class Kotlin {
                    ctor public Kotlin(java.lang.String property1, int arg2);
                    method public java.lang.String getProperty1();
                    method public java.lang.String getProperty2();
                    method public void setProperty2(java.lang.String p);
                    property public final java.lang.String property2;
                  }
                }
                """

        check(
            compatibilityMode = true,
            signatureSource = source,
            api = source
        )
    }
}