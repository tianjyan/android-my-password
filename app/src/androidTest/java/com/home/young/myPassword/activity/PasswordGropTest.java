package com.home.young.myPassword.activity;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.home.young.myPassword.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PasswordGropTest {

    @Rule
    public ActivityTestRule<StartActivity> mActivityTestRule = new ActivityTestRule<>(StartActivity.class);

    @Test
    public void passwordGropTest() {
        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.set_password_skip), withText("不设置密码")));
        appCompatTextView.perform(scrollTo(), click());

        ViewInteraction relativeLayout = onView(
                allOf(withId(R.id.fragment_password_group_add), isDisplayed()));
        relativeLayout.perform(click());

        ViewInteraction relativeLayout2 = onView(
                allOf(withId(R.id.create_passwordGroup_cancel), isDisplayed()));
        relativeLayout2.perform(click());

        ViewInteraction relativeLayout3 = onView(
                allOf(withId(R.id.fragment_password_group_add), isDisplayed()));
        relativeLayout3.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.create_passwordGroup_name), isDisplayed()));
        appCompatEditText.perform(replaceText("测试"), closeSoftKeyboard());

        ViewInteraction relativeLayout4 = onView(
                allOf(withId(R.id.create_passwordGroup_sure), isDisplayed()));
        relativeLayout4.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.fragment_password_group_name), withText("测试"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_password_group_listView),
                                        1),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("测试")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
