require_relative '../../spec_helper'

describe "Fiber.schedule" do
  it "raises a runtime error if no scheduler has been set" do
    -> { Fiber.schedule { } }.should raise_error(RuntimeError, 'No scheduler is available!')
  end

  it "Starts the fiber is a scheduler is available" do
    run = false
    Fiber.set_scheduler FiberSpecs::EmptyScheduler.new
    begin
      Fiber.schedule { run = true }
      run.should == true
    ensure
      Fiber.set_scheduler nil
    end
  end
end
