package com.robert;

public class Sort {

    public static void main(String[] args) {
        int[] array = new int[]{3, 5, 1, 2, 8, 4};
        insertSort(array);
        printArray(array);

        int[] bubbleArray = new int[]{3, 5, 1, 2, 8, 4};
        bubbleSort(bubbleArray, bubbleArray.length);
        printArray(bubbleArray);

        int[] selectArray = new int[]{3, 5, 1, 2, 8, 4};
        selectSort(selectArray, selectArray.length);
        printArray(selectArray);
    }

    public static void selectSort(int[] a, int length) {
        for (int i = 0; i < length; i++) {
            int min = a[i];
            int minIndex = i;
            for (int j = i; j < length; j++) {
                if (min > a[j]) {
                    min = a[j];
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                int temp = a[i];
                a[i] = a[minIndex];
                a[minIndex] = temp;
            }
        }

    }

    public static void bubbleSort(int[] a, int length) {
        for (int i = 0; i < length; i++) {
            boolean flag = false;
            for (int j = 1; j < length - i; j++) {
                if (a[j] < a[j - 1]) {
                    int temp = a[j - 1];
                    a[j - 1] = a[j];
                    a[j] = temp;
                    flag = true;
                }
            }
            if (!flag) {
                break;
            }
        }
    }

    public static void insertSort(int[] a) {
        int length = a.length;
        for (int i = 1; i < length; ++i) {
            int value = a[i];
            int j = i - 1;
            for (; j >= 0; --j) {
                if (a[j] > value) {
                    a[j + 1] = a[j];
                } else {
                    break;
                }
            }
            a[j + 1] = value;
        }
    }

    public static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            if (i != array.length - 1)
                System.out.print(" ");
        }
        System.out.println();
    }
}
