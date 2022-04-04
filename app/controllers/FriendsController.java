package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import model.db.RankFactory;
import model.db.UserFactory;
import model.friends.FriendsFactory;
import model.game.GameStatsFactory;
import play.mvc.*;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class FriendsController extends Controller {
    @Inject
    private UserFactory uf;
    @Inject
    private FriendsFactory ff;
    @Inject
    private RankFactory rf;
    @Inject
    private GameStatsFactory gsf;

    private int user_id;

    /**
     * Default for /friends
     *
     * @return Renders a list of friends for a logged in User.
     */
    public Result showFriends() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            List<FriendsFactory.Friend> friends = ff.getFriendList(user_id);
            List<UserFactory.User> requests = ff.getFriendRequests(user_id);
            return ok(views.html.friends.render(friends, requests));
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Default for /users/:friend. Shows a profile of a given User based on their relationship to the logged in user.
     * If the profile's User is friends with the logged in User, the logged in User can see more details.
     *
     * @param friend Name of a User.
     * @return Renders either a general user's profile page or a more detailed friend's profile page.
     */
    public Result showFriend(String friend) {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            if (!uf.nameExists(friend)) {
                return redirect("/users");
            }
            UserFactory.User user_friend = uf.fromDBWithName(friend).get(0);
            RankFactory.Rank rank = rf.getRankByPointsOverall(user_friend.getPoints_overall());
            if (ff.areFriends(user_id, user_friend.getUser_id())) {
                if (gsf.hasStats(user_friend.getUser_id())) {
                    GameStatsFactory.GameStatsGroup gsg = gsf.getGameStatsGroup(user_friend.getUser_id());
                    return ok(views.html.profile_friend.render(user_friend, rank, gsg));
                } else {
                    return ok(views.html.profile_friend.render(user_friend, rank, null));
                }
            } else {
                return ok(views.html.profile.render(user_friend,
                        ff.hasRequest(user_id, user_friend.getUser_id()),
                        ff.hasSentRequest(user_id, user_friend.getUser_id())
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Default for POST /friendsRequest. Creates a friendship invitation.
     *
     * @return Sends friendRequest.
     */
    public Result requestFriend() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        JsonNode json = request().body().asJson();
        int friend_id = json.get("userid").asInt();

        try {
            user_id = Integer.parseInt(session("user_id"));
            if (!ff.areFriends(user_id, friend_id) || !ff.hasRequest(user_id, friend_id)) {
                ff.sendFriendRequest(user_id, friend_id);
            }
            return ok();
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Accepts a friend request.
     *
     * @return Accepts a friend request, returns to profile.
     */
    public Result acceptFriend() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        JsonNode json = request().body().asJson();
        int friend_id = json.get("userid").asInt();

        try {
            if (!ff.areFriends(user_id, friend_id) && ff.hasRequest(user_id, friend_id)) {
                ff.acceptFriendRequest(user_id, friend_id);
            }
            return ok();

        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Deletes a friendship between two users.
     *
     * @return Deletes friendship, returns to profile.
     */
    public Result deleteFriend() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        JsonNode json = request().body().asJson();
        int friend_id = json.get("userid").asInt();
        try {
            if (ff.areFriends(user_id, friend_id)) {
                ff.deleteFriend(user_id, friend_id);
            }
            return ok();
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }


}
