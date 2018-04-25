public class MaxSpan {
  public static void main(String[] args) {
    int[] nums1 = {1, 2, 1, 1, 3};
    maxSpan(nums1);
    int[] nums2 = {1, 4, 2, 1, 4, 1, 4};
    maxSpan(nums2);
    int[] nums3 = {1, 4, 2, 1, 4, 4, 4};
    maxSpan(nums3);
  }

  public static void maxSpan(int[] nums) {
    int len = nums.length;
    int max = 0;
    for (int i = 0; i < len; i++) {
      for (int ii = len - 1; ii >= 0; ii--) {
        if (nums[i] == nums[ii] && ii + 1 - i >= max) {
          max = ii + 1 - i;
          break;
        }
      }
    }
    System.out.println(max);
  }
}
