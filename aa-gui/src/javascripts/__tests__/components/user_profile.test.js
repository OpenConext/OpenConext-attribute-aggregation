import React from "react";
import {shallow} from "enzyme";
import UserProfile from "../../components/user_profile";
import start from "../base";
start();

test("UserProfile with email", () => {
    const currentUser = {person: {email: "test@org.net", guest: true}};
    const userProfile = shallow(
        <UserProfile currentUser={currentUser}/>
    );

    expect(userProfile.contains(<span>Name:</span>)).toBe(true);

});