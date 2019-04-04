#include <jni.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define THRESHOLD1 0.1
#define THRESHOLD2 0.25

float pitch_detect(const short *sample, int length_, int samplerate) {
	int length = length_ / 2;
	float *diff = malloc(length * sizeof(float));
	memset(diff, 0, length * sizeof(float));
	for (int t = 0; t < length; t++)
		for (int i = 1; i <= length; i++)
			diff[t] += pow(sample[i] - sample[i + t], 2);

	float *cmn = malloc(length * sizeof(float));
	memset(cmn, 0, length * sizeof(float));
	cmn[0] = 1;
	float denominator = 0;
	for (int t = 1; t < length; t++){
		denominator += diff[t];
		if (denominator != 0)
			cmn[t] = diff[t] * t / denominator;
		else
			cmn[t] = 1;
	}

	free(diff);

	int period = 0;
	// Find the first local minimum below threshold
	for (int i = 1; i < length - 1; i++){
		if (cmn[i] <= cmn[i - 1] && cmn[i] <= cmn[i + 1] && cmn[i] <= THRESHOLD1) {
			period = i;
			break;
		}
	}

	// If not found, then find smallest local minimum under greater threshold.
	if (period == 0) {
		float minimum = MAXFLOAT;
		for (int i = 1; i < length - 1; i++) {
			if (cmn[i] <= minimum && cmn[i] <= cmn[i - 1] && cmn[i] <= cmn[i + 1] && cmn[i] <= THRESHOLD2) {
				period = i;
				minimum = cmn[i];
			}
		}
	}
	free(cmn);

	if (period == 0)
		return 0;

	return (float)samplerate / period;
}

JNIEXPORT jfloat JNICALL
Java_com_mustupid_pitchgram_PitchDetector_detectPitch(JNIEnv* env, jobject instance,
                                                      jshortArray sample_, jint samplerate) {
	jshort *sample = (*env)->GetShortArrayElements(env, sample_, 0);
	jsize length = (*env)->GetArrayLength(env, sample_);
	jfloat result = pitch_detect(sample, length, samplerate);
	(*env)->ReleaseShortArrayElements(env, sample_, sample, 0);
	return result;
}
