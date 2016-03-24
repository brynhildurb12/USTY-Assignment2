package com.ru.usty.scheduling.visualization;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.BufferUtils;
import com.ru.usty.scheduling.process.Process;

import java.nio.FloatBuffer;

public class SchedulingGraphics implements ApplicationListener{

	FloatBuffer vertexBuffer;

	@Override
	public void create() {
		Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		Gdx.gl11.glClearColor(0.4f, 0.6f, 1.0f, 1.0f);
		
		vertexBuffer = BufferUtils.newFloatBuffer(8);
		vertexBuffer.put(new float[] {-0.5f,0.0f, -0.5f,1.0f, 0.5f,0.0f, 0.5f,1.0f});
		vertexBuffer.rewind();
	}

	@Override
	public void dispose() {
	// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
	// TODO Auto-generated method stub
	}

	private void update() {
		if(!TestSuite.update()) {
			Gdx.app.exit();
		}
	}

	private void display() {

		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW);
		Gdx.gl11.glLoadIdentity();
		Gdx.glu.gluOrtho2D(Gdx.gl10, 0, Gdx.graphics.getWidth(), 0, Gdx.graphics.getHeight());

		Gdx.gl11.glColor4f(0.6f, 0.0f, 0.0f, 1.0f);

		Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, vertexBuffer);

		Gdx.gl11.glPushMatrix();
		Gdx.gl11.glTranslatef(0f, 30f, 0.0f);

		for(Process process : TestSuite.getProcesses()) {

			Gdx.gl11.glTranslatef(30f, 0.0f, 0.0f);

			float columnHeight = 0.1f * (float)(process.getTotalServiceTime());

			Gdx.gl11.glColor4f(0.6f, 0.0f, 0.0f, 1.0f);
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glScalef(10.0f, columnHeight, 100.0f);
			Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			Gdx.gl11.glPopMatrix();

			columnHeight = 0.1f * (float)(process.getElapsedExecutionTime());

			Gdx.gl11.glColor4f(0.0f, 0.6f, 0.0f, 1.0f);
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glScalef(10.0f, columnHeight, 100.0f);
			Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			Gdx.gl11.glPopMatrix();
		}

		Gdx.gl11.glPopMatrix();
	}

	@Override
	public void render() {

		update();
		display();
	}

	@Override
	public void resize(int arg0, int arg1) {
	// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
	// TODO Auto-generated method stub

	}

}