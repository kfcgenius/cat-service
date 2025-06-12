package cat_service.constants;

public interface KafkaAction {
  String CREATE_CAT = "CREATE_CAT";
  String LIKE_CAT = "LIKE_CAT";
  String DISLIKE_CAT = "DISLIKE_CAT";
  String DELETE_CAT = "DELETE_CAT";
  String GET_USER_CATS = "GET_USER_CATS";
  String GET_RANDOM_CAT = "GET_RANDOM_CAT";
  String GET_OR_CREATE_USER = "GET_OR_CREATE_USER";
}
